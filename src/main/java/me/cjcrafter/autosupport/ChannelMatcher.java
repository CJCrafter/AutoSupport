package me.cjcrafter.autosupport;

import java.util.function.Predicate;

/**
 * Returns <code>true</code> when the auto-support is allowed to respond in
 * the given channel id.
 */
public interface ChannelMatcher extends Predicate<String> {
}
