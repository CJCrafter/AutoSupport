package me.cjcrafter.autosupport;

import java.util.function.Predicate;

/**
 * This interface is used to determine if an input string should be checked
 * as a question. If you use a bot channel, like <code>#talk-to-bot</code>,
 * then you should see {@link #ALWAYS}. If you don't want the bot to respond
 * to every message, then you should use {@link #QUESTION}.
 */
@FunctionalInterface
public interface Activator extends Predicate<String> {

    /**
     * Returns <code>true</code> for all input.
     */
    Activator ALWAYS = input -> true;

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
     */
    Activator QUESTION = Activator::isQuestion;


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
        String[] sentences = input.split("\\.");
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
            switch (s) {
                case "no", "don't", "dont", "wont", "won't", "cant", "can't", "didnt", "didn't" -> isNegative = true;
                case "work" -> {
                    // check negative BEFORE "work" since
                    if (isNegative)
                        return true;
                }
            }
        }

        boolean isFirstQuestion = switch (split[0]) {
            case "why", "what", "can", "is", "how", "where", "do", "does", "which", "am" -> true;
            default -> false;
        };

        // Many people like to preface there question with a word:
        // "Hi, can I get help with this?"
        // "Oh is that possible?"
        // "Yeah where can I find that?"
        // So we check the second word. This will lead to some false positives,
        // but it is a small price to pay since people can delete the bot message.
        boolean isSecondQuestion = split.length > 1 && switch (split[1]) {
            case "why", "what", "can", "is", "how", "where", "do", "does", "which", "am" -> true;
            default -> false;
        };

        return isFirstQuestion || isSecondQuestion;
    }
}
