package me.cjcrafter.autosupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class KeywordMatcher implements StringMatcher {

    private final List<String> keys;

    public KeywordMatcher(String... keys) {
        this(Arrays.asList(keys));
    }

    public KeywordMatcher(Collection<String> keys) {
        this.keys = new ArrayList<>(keys);
    }

    @Override
    public boolean test(String input) {
        return keys.stream().anyMatch(input::contains);
    }
}
