package me.cjcrafter.autosupport.discord;

import me.cjcrafter.autosupport.ChannelMatcher;

import java.util.List;

/**
 * Matches a list of {@link net.dv8tion.jda.api.entities.channel.concrete.TextChannel} ids. The
 * ids are <b>NOT</b> validated, so it is important to check that your ids
 * actually exist in Discord.
 *
 * @param whitelist  true -> only channels from list, false -> all channels BUT from the list.
 * @param channelIds The list of channel ids.
 */
public record DiscordChannelMatcher(boolean whitelist, List<String> channelIds) implements ChannelMatcher {

    @Override
    public boolean test(String channel) {
        if (whitelist)
            return channelIds.contains(channel);
        else
            return !channelIds.contains(channel);
    }
}
