package me.cjcrafter.autosupport.discord;

import me.cjcrafter.autosupport.SupportData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    private DiscordHelper() {
        throw new IllegalStateException("Nobody may instantiate " + DiscordHelper.class);
    }

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

    public static List<Button> getButtons(SupportData support) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.success(ACCEPT_KEY, "This answered my question"));
        buttons.add(Button.danger(REJECT_KEY, "This didn't answer my question"));
        Button extra = getButton(support);
        if (extra != null)
            buttons.add(extra);
        return buttons;
    }

    public static Button getButton(SupportData support) {
        if (support.getButton() == null)
            return null;

        String label = support.getButton().label();
        String url = support.getButton().link();

        return Button.link(url, label);
    }

    public static boolean isChannelWhitelist(SupportData support, TextChannel channel) {
        return support.getChannelMatcher().test(channel.getId());
    }

}
