package me.cjcrafter.autosupport;

import me.cjcrafter.autosupport.discord.DiscordChannelMatcher;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class which holds data from the json files. Contains information like:
 * <ul>
 *     <li>When to trigger</li>
 *     <li>Where to trigger</li>
 *     <li>Who to respond to</li>
 *     <li>What information to respond with</li>
 *     <li>etc.,,</li>
 * </ul>
 */
public class SupportData {

    /**
     * Add your own implementations here if you feel the defaults are not good
     * enough.
     */
    public static final Map<String, Activator> ACTIVATORS = new HashMap<>();

    static {
        ACTIVATORS.put("ALWAYS", Activator.ALWAYS);
        ACTIVATORS.put("QUESTION", Activator.QUESTION);
    }

    /**
     * Record to store information about the extra button.
     */
    public record ButtonData(String label, String link) {}

    private Activator activator;
    private boolean isOnlyUnverified;
    private boolean isDeleteAfterAnswer;
    private ChannelMatcher channelMatcher;
    private Map<StringMatcher, Integer> keys;
    private int keyThreshold;
    private String question;
    private List<String> answer;
    private String media;
    private ButtonData button;

    /**
     * Constructor for the clinically insane.
     */
    public SupportData(Activator activator, boolean isOnlyUnverified, boolean isDeleteAfterAnswer,
                       ChannelMatcher channelMatcher, Map<StringMatcher, Integer> keys, int keyThreshold,
                       String question, List<String> answer, String media, ButtonData button) {

        this.activator = activator;
        this.isOnlyUnverified = isOnlyUnverified;
        this.isDeleteAfterAnswer = isDeleteAfterAnswer;
        this.channelMatcher = channelMatcher;
        this.keys = keys;
        this.keyThreshold = keyThreshold;
        this.question = question;
        this.answer = answer;
        this.media = media;
        this.button = button;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SupportData(Map json) {
        this.activator = ACTIVATORS.get(((String) json.getOrDefault("Activator", "QUESTION")).toUpperCase(Locale.ROOT));
        this.isOnlyUnverified = (Boolean) json.getOrDefault("Only_Unverified", false);
        this.isDeleteAfterAnswer = (Boolean) json.getOrDefault("Delete_After_Answer", true);
        this.keyThreshold = ((Long) json.getOrDefault("Key_Threshold", 1L)).intValue();
        this.question = (String) json.get("Question");
        this.answer = (List<String>) json.get("Message");
        this.media = (String) json.get("Media");

        // Extra button option, or null
        Map<String, String> buttonData = (Map<String, String>) json.get("Button");
        if (buttonData != null) {
            this.button = new ButtonData(buttonData.get("Text"), buttonData.get("Link"));
        }

        // Load keys
        // Format: key1,key2,key3:weight
        this.keys = new HashMap<>();
        for (String key : (List<String>) json.get("Keys")) {
            String[] split = key.split(":");
            List<String> matches = Arrays.asList(split[0].split(","));
            int weight = Integer.parseInt(split[1]);

            this.keys.put(new KeywordMatcher(matches), weight);
        }

        // Only in specific channels
        boolean channelWhitelist = (Boolean) json.getOrDefault("Channel_Whitelist", true);
        List<String> channels = (List<String>) json.getOrDefault("Channels", Collections.emptyList());
        this.channelMatcher = new DiscordChannelMatcher(channelWhitelist, channels);
    }

    public Activator getActivator() {
        return activator;
    }

    public void setActivator(Activator activator) {
        this.activator = activator;
    }

    public boolean isOnlyUnverified() {
        return isOnlyUnverified;
    }

    public void setOnlyUnverified(boolean onlyUnverified) {
        isOnlyUnverified = onlyUnverified;
    }

    public boolean isDeleteAfterAnswer() {
        return isDeleteAfterAnswer;
    }

    public void setDeleteAfterAnswer(boolean deleteAfterAnswer) {
        isDeleteAfterAnswer = deleteAfterAnswer;
    }

    public ChannelMatcher getChannelMatcher() {
        return channelMatcher;
    }

    public void setChannelMatcher(ChannelMatcher channelMatcher) {
        this.channelMatcher = channelMatcher;
    }

    public Map<StringMatcher, Integer> getKeys() {
        return keys;
    }

    public void setKeys(Map<StringMatcher, Integer> keys) {
        this.keys = keys;
    }

    public int getKeyThreshold() {
        return keyThreshold;
    }

    public void setKeyThreshold(int keyThreshold) {
        this.keyThreshold = keyThreshold;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public ButtonData getButton() {
        return button;
    }

    public void setButton(ButtonData button) {
        this.button = button;
    }

    /**
     * Returns <code>true</code> when the given question matches this
     * {@link SupportData}.
     *
     * @param question The lowercase user message.
     * @return true if the question matches THIS data.
     */
    public int score(String question) {
        AtomicInteger count = new AtomicInteger();
        keys.forEach((matcher, weight) -> {
            if (matcher.test(question))
                count.addAndGet(weight);
        });

        return count.get();
    }
}