package me.cjcrafter.autosupport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This interface is used to determine if an input string should be checked
 * as a question. If you use a bot channel, like <code>#talk-to-bot</code>,
 * then you should see {@link #ALWAYS}. If you don't want the bot to respond
 * to every message, then you should use {@link #QUESTION}.
 *
 * <p>When implementing this interface, make sure to add it to the registry in
 * {@link SupportData#ACTIVATORS}.
 */
@FunctionalInterface
public interface Activator extends Predicate<String> {

    /**
     * List of question words used in {@link #isQuestion(String)}.
     */
    Set<String> QUESTION_WORDS = new HashSet<>(Arrays.asList("why", "what", "can", "is", "how", "where", "do", "does",
            "which", "am", "are"));

    /**
     * Returns <code>true</code> for all input.
     */
    Activator ALWAYS = input -> true;

    /**
     * Returns <code>true</code> for any input that is a question.
     * @see #isQuestion(String)
     */
    Activator QUESTION = Activator::isQuestion;

    /**
     * Returns <code>true</code> for any input that <i>appears to be a
     * question</i>. More specifically, this returns <code>true</code> when
     * any one of these conditions are met:
     * <ul>
     *     <li>Input uses question marks</li>
     *     <li>First word is a question word</li>
     *     <li>Second word is a question word</li>
     *     <li>If the input passed the "car no go" check</li>
     * </ul>
     *
     * @param input The non-null message from the user.
     * @return true if the message is a question.
     */
    static boolean isQuestion(String input) {
        if (input.startsWith("¿") || input.endsWith("?"))
            return true;

        // Quick optimization for empty strings, usually caused by users
        // typing "...".
        if (input.isEmpty())
            return false;

        // Sometimes people go for a full on introduction before asking their
        // question, like: "Hello, I have a problem with the plugin. Is there
        // a way..."
        String[] sentences = input.split("[.,!]");
        if (sentences.length > 1) {
            for (String sentence : sentences)
                if (isQuestion(sentence))
                    return true;

            return false;
        }

        // This is the "car no go" check. Since a lot of non-english-speakers
        // tend to be asking for support, this check helps for "this no work"
        // type questions.
        String[] split = input.split(" ");
        boolean isNegative = false;
        for (String s : split) {
            switch (trim(s)) {
                case "no", "don't", "dont", "wont", "won't", "cant", "can't", "didnt", "didn't", "not" -> isNegative = true;
                case "work", "working", "works" -> {
                    // check negative BEFORE "work" since
                    if (isNegative)
                        return true;
                }
            }
        }

        boolean isFirstQuestion = QUESTION_WORDS.contains(trim(split[0]));

        // Many people like to preface there question with a word:
        // "Hi, can I get help with this?"
        // "Oh is that possible?"
        // "Yeah where can I find that?"
        // So we check the second word. This will lead to some false positives,
        // but it is a small price to pay since people can delete the bot message.
        boolean isSecondQuestion = split.length > 1 && QUESTION_WORDS.contains(trim(split[1]));

        return isFirstQuestion || isSecondQuestion;
    }

    /**
     * Trims any non-alphabetical characters from the beginning and end of the
     * given string. If the given string doesn't have any alphabetical
     * characters, the string is returned unmodified.
     *
     * @param str The non-null string to trim.
     * @return The trimmed string.
     */
    static String trim(String str) {
        int start;
        for (start = 0; start < str.length(); start++) {
            if (Character.isAlphabetic(str.charAt(start)))
                break;
        }

        int stop;
        for (stop = str.length() - 1; stop >= 0; stop--) {
            if (Character.isAlphabetic(str.charAt(stop)))
                break;
        }

        // Whenever the string is filled with non-alphabetical characters, we
        // simply return whatever we started with.
        if (start >= stop)
            return str;

        return str.substring(start, stop + 1);
    }
}
