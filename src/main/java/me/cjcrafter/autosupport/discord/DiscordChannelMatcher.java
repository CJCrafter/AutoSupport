package me.cjcrafter.autosupport.discord;

import me.cjcrafter.autosupport.ChannelMatcher;

import java.util.List;

public record DiscordChannelMatcher(boolean whitelist, List<String> channelIds) implements ChannelMatcher {

    @Override
    public boolean test(String channel) {
        if (whitelist)
            return channelIds.contains(channel);
        else
            return !channelIds.contains(channel);
    }
}
