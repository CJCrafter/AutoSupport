package me.cjcrafter.autosupport.discord;

import me.cjcrafter.autosupport.FileHelper;
import me.cjcrafter.autosupport.SupportData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Implementation for Discord using JDA's {@link ListenerAdapter}. This setup
 * is designed as a "plugin-and-play" system, although you will need to write
 * all of your json files.
 */
public class DiscordImpl extends ListenerAdapter {

    private Set<String> staffRoles;
    private final Consumer<String> logger;
    private String logsChannel;
    private String verifiedRole;
    private final List<SupportData> supportList;

    @SuppressWarnings("unchecked")
    public DiscordImpl(URL folder, Consumer<String> logger) {
        this.supportList = new ArrayList<>();
        this.logger = logger == null ? str -> { /* do nothing */ } : logger;

        JSONParser parser = new JSONParser();
        FileHelper.forEachResource(folder, file -> {
            try {
                JSONObject json = (JSONObject) parser.parse(new InputStreamReader(Files.newInputStream(file)));

                // config.json is a special file
                if ("config.json".equals(file.getFileName().toString())) {
                    staffRoles = new HashSet<>((List<String>) json.get("Staff_Roles"));
                    logsChannel = (String) json.get("Logs_Channel");
                    verifiedRole = (String) json.get("Verified_Role");
                    return;
                }

                String name = file.getFileName().toString();
                if (name.endsWith(".json"))
                    name = name.substring(0, name.length() - ".json".length());

                SupportData temp = new SupportData(name, json);
                supportList.add(temp);

            } catch (IOException | ParseException e) {
                throw new InternalError(e);
            }
        });

        if (staffRoles == null)
            throw new IllegalArgumentException("Could not find config.json, or it had no staff roles");
    }

    /**
     * Returns the <code>true</code> if the given member is a staff member. The
     * staff roles can be changes in your <code>config.json</code> file in your
     * auto-support directory.
     *
     * @param member The non-null member to check.
     * @return true if the member is staff.
     */
    public boolean isStaff(Member member) {
        return member.getRoles().stream().anyMatch(role -> staffRoles.contains(role.getId()));
    }

    /**
     * Returns a modifiable list of currently registered support data.
     *
     * @return The non-null support list.
     */
    public List<SupportData> getSupportList() {
        return supportList;
    }

    /**
     * Returns a stream of support data that matches the arguments. Note that
     * the stream <i>may</i> be empty.
     *
     * @param channel  Which channel the support data is for.
     * @param verified Only include support for verified users.
     * @return The non-null stream of options.
     */
    public Stream<SupportData> getSupportFor(String channel, boolean verified) {
        return getSupportList().stream()
                .filter(support -> support.getChannelMatcher().test(channel))
                .filter(support -> !support.isOnlyUnverified() || verified);
    }

    /**
     * Returns the first support data found that matches the arguments. If no
     * support data is found, this method will return null.
     *
     * @param channel  Which channel the support data is for.
     * @param verified Only include support for verified users.
     * @param name     The name of the file.
     * @return The support data, or null.
     */
    public SupportData getByName(String channel, boolean verified, String name) {
        return getSupportFor(channel, verified)
                .filter(support -> support.getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel() instanceof TextChannel textChannel)
            receiveMessage(textChannel, event.getAuthor(), event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getChannel() instanceof TextChannel textChannel)
            receiveMessage(textChannel, event.getAuthor(), event.getMessage());
    }

    /**
     * Handles the logic of a message being sent in a channel.
     *
     * @param channel The non-null channel where the message was sent.
     * @param sender  The non-null user who sent the message.
     * @param message The non-null message.
     */
    public void receiveMessage(TextChannel channel, User sender, Message message) {
        String question = message.getContentRaw().toLowerCase(Locale.ROOT);
        Member member = channel.getGuild().getMember(sender);
        if (member == null) {
            logger.accept(sender + " left the server?");
            return;
        }

        // When a message starts with '?', then we will force it to be a
        // question. This is most helpful for developers manually asking the
        // bot to autofill an answer.
        boolean forceQuestion = question.startsWith("?") && !question.equals("?");

        // Sometimes admins will reply to a message and manually trigger a
        // specific auto-support message. In this case, the auto-support should
        // reply to the USER's message INSTEAD OF the ADMIN's message.
        if (forceQuestion && message.getMessageReference() != null) {
            Message clientMessage = message.getMessageReference().resolve().complete();
            logger.accept("Actually replying to '" + clientMessage + "' instead of '" + message + "'");
            message.delete().queueAfter(3L, TimeUnit.SECONDS);
            message = clientMessage;
        }

        // Block the bot from replying to staff members, unless the message
        // starts with '?'.
        if (!forceQuestion && isStaff(member))
            return;

        // Store the best auto support
        SupportData best = null;
        int maxScore = Integer.MIN_VALUE;

        for (SupportData data : supportList) {
            if (!DiscordHelper.isChannelWhitelist(data, channel))
                continue;
            if (!forceQuestion && !data.getActivator().test(question))
                continue;
            if (data.isOnlyUnverified() && member.getRoles().stream().anyMatch(role -> verifiedRole.equals(role.getId())))
                continue;

            int score = data.score(question);
            if (score < data.getKeyThreshold())
                continue;

            // Only save if the response is a better match
            if (score > maxScore) {
                best = data;
                maxScore = score;
            }
        }

        // Send the message/buttons
        if (best != null) {
            EmbedBuilder embed = DiscordHelper.getEmbed(best);
            List<Button> buttons = DiscordHelper.getButtons(best);

            message.replyEmbeds(embed.build()).addActionRow(buttons).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        Button button = event.getButton();
        boolean accept = DiscordHelper.ACCEPT_KEY.equals(button.getId());
        if (!accept && !DiscordHelper.REJECT_KEY.equals(button.getId())) {
            logger.accept("Button wasn't auto support '" + button.getId() + "'");
            return;
        }

        // Gather data from the event
        Message answer = event.getMessage();
        Message question = answer.getMessageReference() == null ? null : answer.getMessageReference().resolve().complete();
        MessageEmbed embed = answer.getEmbeds().isEmpty() ? null : answer.getEmbeds().get(0);
        Member member = event.getMember();
        boolean shouldDelete = embed != null && embed.getFooter() != null && DiscordHelper.DELETE_FOOTER.equals(embed.getFooter().getText());

        // Since we already did the button check, ALL of these conditions SHOULD
        // be impossible. That being said, CJCrafter has a history of breaking
        // the poor CubeDev Bot, so let's be thorough.
        if (question == null || member == null || embed == null || embed.getTitle() == null) {
            logger.accept("Something went wrong... one of these is null??? " + question + ", " + member + ", " + embed);
            return;
        }

        // First we need to check if the correct person is pressing the button.
        // Only staff and the person who asked may press the button.
        if (!question.getAuthor().equals(member.getUser()) && member.getRoles().stream().noneMatch(role -> staffRoles.contains(role.getId()))) {
            event.deferEdit().queue();
            logger.accept("Wrong person tried to push the button");
            return;
        }

        // The unix-format time (unix is measured in seconds)
        boolean isDelete = shouldDelete || !accept;
        long unix = System.currentTimeMillis() / 1000L + (accept ? 30L * 60L : 8L);
        String deleteInfo = "Message will be removed <t:" + unix + ":R>"; // R = relative

        // Disabling the 2 accept and reject buttons. Leaving any extra buttons.
        Button[] disabledButtons = answer.getButtons().stream().map(temp -> {
            if (DiscordHelper.ACCEPT_KEY.equals(temp.getId()) || DiscordHelper.REJECT_KEY.equals(temp.getId()))
                return temp.asDisabled();
            return temp;
        }).toArray(Button[]::new);

        // Handle deleting the question/answer.
        if (accept && shouldDelete) question.delete().queueAfter(30L, TimeUnit.MINUTES);
        if (isDelete) {
            if (accept) {
                EmbedBuilder edit = new EmbedBuilder(embed).appendDescription("\n\n" + deleteInfo).setFooter(null);
                answer.editMessageEmbeds(edit.build()).setActionRow(disabledButtons).queue(m -> m.delete().queueAfter(30L, TimeUnit.MINUTES));
            } else {
                EmbedBuilder edit = new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Auto Support")
                        .appendDescription("Sorry about my poor answer. " + deleteInfo);
                answer.editMessageEmbeds(edit.build()).setActionRow(disabledButtons).queue(m -> m.delete().queueAfter(9L, TimeUnit.SECONDS));
            }
        }

        // Let the event know the button press was successful. Otherwise, the
        // user would see, "This interaction failed" error message. setActionRow()
        // deletes the buttons so the user cannot press them again.
        event.deferEdit().setActionRow(disabledButtons).queue();

        // Add message to logs
        // Log the message in #discord-logs
        if (logsChannel != null) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Auto Support Feedback")
                    .setDescription("The user was " + (accept ? "happy" : "unhappy") + " with the response. " + (isDelete ? deleteInfo : ""))
                    .setImage(member.getAvatarUrl())
                    .setColor(accept ? Color.GREEN : Color.RED)
                    .addField("User", member.getAsMention(), true)
                    .addField("Channel", answer.getChannel().getAsMention(), true)
                    .addField("Message Link", answer.getJumpUrl(), false)
                    .addField("Input", question.getContentDisplay(), false)
                    .addField("Inferred", embed.getTitle(), false);

            TextChannel channel = event.getGuild().getTextChannelById(logsChannel);
            channel.sendMessageEmbeds(builder.build()).queue();
        }
    }
}