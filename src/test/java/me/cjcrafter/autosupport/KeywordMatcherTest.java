package me.cjcrafter.autosupport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class KeywordMatcherTest {

    static Stream<Arguments> provide_colors() {
        return Stream.of(
                Arguments.of("Can I add colors to my lore?", true),
                Arguments.of("Does WeaponMechanics support hex codes", true),
                Arguments.of("How can I use color codes in the item display", true),
                Arguments.of("Are rgb values valid?", true),
                Arguments.of("is there a way to use codes like #ffffff in my chat", true),

                Arguments.of("Combat border is not working", false),
                Arguments.of("Disable guns in regions", false),
                Arguments.of("Where can I buy the procosmetics source code", false)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Can I add colors to my lore?",
            "Does WeaponMechanics support hex codes",
            "How can I use color codes in the item display",
            "Are rgb values valid?"
    })
    void test_colors(String input, boolean expected) {
        StringMatcher matcher = new KeywordMatcher("rgb", "color", "hex", "#ffffff");
        boolean actual = matcher.test(input.toLowerCase(Locale.ROOT));

        assertEquals(expected, actual);
    }
}
