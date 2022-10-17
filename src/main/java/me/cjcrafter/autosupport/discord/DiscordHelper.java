package me.cjcrafter.autosupport.discord;

import me.cjcrafter.autosupport.SupportData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Since AutoSupport is <i>technically</i> platform independent, discord code
 * is not directly implemented. That being said, I highly doubt this will ever
 * be used outside of discord, so I might as well include this for easier
 * implementations.
 */
public final class DiscordHelper {

    public static final String ACCEPT_KEY = "auto-support-accept";
    public static final String REJECT_KEY = "auto-support-reject";
    public static final String DELETE_FOOTER = "Pressing the buttons will delete this message";

    /**
     * Don't let anyone instantiate this utility class.
     */
    private DiscordHelper() {
        throw new IllegalStateException("Nobody may instantiate " + DiscordHelper.class);
    }

    /**
     * Constructs an {@link EmbedBuilder} intended for the user who asks the
     * question.
     *
     * @param support The non-null support data with the message content.
     * @return The non-null Discord embed.
     */
    public static EmbedBuilder getEmbed(SupportData support) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(support.getQuestion())
                .setColor(Color.YELLOW)
                .setImage(support.getMedia());

        if (support.isDeleteAfterAnswer())
            embed.setFooter(DELETE_FOOTER);

        support.getAnswer().forEach(embed::appendDescription);
        return embed;
    }

    /**
     * Returns two or three buttons. The first two are the accept/reject
     * buttons. The optional third button is for more information.
     *
     * @param support The non-null support data.
     * @return The non-null list of two or three buttons.
     */
    public static List<Button> getButtons(SupportData support) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.success(ACCEPT_KEY, "This answered my question"));
        buttons.add(Button.danger(REJECT_KEY, "This didn't answer my question"));
        Button extra = getButton(support);
        if (extra != null)
            buttons.add(extra);
        return buttons;
    }

    /**
     * Returns the "more-information" button.
     *
     * @param support The non-null support data.
     * @return The more-information button, or null.
     */
    public static Button getButton(SupportData support) {
        if (support.getButton() == null)
            return null;

        String label = support.getButton().label();
        String url = support.getButton().link();

        return Button.link(url, label);
    }

    /**
     * Returns <code>true</code> if the support message can be sent in the
     * given channel.
     *
     * @param support The non-null support data.
     * @param channel The non-null text channel to check.
     * @return true if the message can be sent in the channel.
     */
    public static boolean isChannelWhitelist(SupportData support, TextChannel channel) {
        return support.getChannelMatcher().test(channel.getId());
    }
}
