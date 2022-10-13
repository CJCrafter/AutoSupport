package me.cjcrafter.autosupport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ActivatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Why is my foot on fire?",
            "Is a window open",
            "where are the beans",
            "how is it that thanos was stronger in the second movie",
            "Honestly...I am not even sure this is a question?",
            "Firstly, I will introduce myself. Is the sky blue",
            "can I go to the bathroom"
    })
    void test_isQuestion(String input) {
        boolean actual = Activator.isQuestion(input.toLowerCase(Locale.ROOT));
        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "my dog is brown",
            "I have 9 toes",
            "Officer, I drop-kicked that child in SELF DEFENSE",
            "If I had more to say...",
            "Now, isn't that funny",
            "a"
    })
    void test_isNotQuestion(String input) {
        boolean actual = Activator.isQuestion(input.toLowerCase(Locale.ROOT));
        assertFalse(actual);
    }

    @ParameterizedTest
    @CsvSource({
            "__trim__,trim",
            "Hello!,Hello",
            " white space ,white space",
            "that is weird...,that is weird",
            "change nothing,change nothing",
            "?@&#^  ,?@&#^  "
    })
    void test_trim(String input, String expected) {
        String actual = Activator.trim(input);
        assertEquals(expected, actual);
    }
}
