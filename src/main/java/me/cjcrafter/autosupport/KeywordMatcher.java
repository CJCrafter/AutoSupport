package me.cjcrafter.autosupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class utilizes the {@link String#contains(CharSequence)} method to
 * check if keywords or key phrases are present in a given string. Note that
 * this fails to account for any mis-spelling.
 */
public class KeywordMatcher implements StringMatcher {

    private final List<String> keys;

    /**
     * Constructor for programmers.
     *
     * @param keys The non-null array of keys.
     */
    public KeywordMatcher(String... keys) {
        this(Arrays.asList(keys));
    }

    /**
     * Constructor for json files.
     *
     * @param keys The non-null list of keys. A new list is created.
     */
    public KeywordMatcher(Collection<String> keys) {
        this.keys = new ArrayList<>(keys.stream().map(String::toLowerCase).toList());
    }

    @Override
    public boolean test(String input) {
        return keys.stream().anyMatch(input::contains);
    }
}
