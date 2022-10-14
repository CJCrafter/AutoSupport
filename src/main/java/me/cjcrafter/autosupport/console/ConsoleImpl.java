package me.cjcrafter.autosupport.console;

import me.cjcrafter.autosupport.FileHelper;
import me.cjcrafter.autosupport.SupportData;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Incomplete implementation, similar to {@link me.cjcrafter.autosupport.discord.DiscordImpl},
 * which allows you to test your support messages before testing the proper
 * implementation. Useful for when running a Discord Bot (or whatever your
 * environment may be) is time-consuming.
 */
public class ConsoleImpl {

    private final Scanner scanner;
    private final List<SupportData> supportList;

    public ConsoleImpl(URL folder, InputStream in) {
        this.scanner = new Scanner(in);
        this.supportList = new ArrayList<>();

        JSONParser parser = new JSONParser();
        FileHelper.forEachResource(folder, file -> {
            try {
                if ("config.json".equals(file.getFileName().toString()))
                    return;

                JSONObject json = (JSONObject) parser.parse(new InputStreamReader(Files.newInputStream(file)));
                SupportData temp = new SupportData(json);
                supportList.add(temp);

            } catch (IOException | ParseException e) {
                throw new InternalError(e);
            }
        });
    }

    /**
     * Completely steals the thread and locks execution, but eyyy testing!
     */
    public void start() {
        while (true) {
            String question = scanner.nextLine().toLowerCase(Locale.ROOT);
            receiveMessage(question);
        }
    }

    /**
     * Handles message calculations.
     *
     * @param question The non-null, lowercase question.
     */
    public void receiveMessage(String question) {

        // Store the best auto support
        SupportData best = null;
        int maxScore = Integer.MIN_VALUE;

        for (SupportData data : supportList) {
            if (!data.getActivator().test(question))
                continue;

            int score = data.score(question);
            if (score < data.getKeyThreshold())
                continue;

            // Only save if the response is a better match
            if (score > maxScore) {
                best = data;
                maxScore = score;
            }
        }

        // Send the message/buttons
        if (best != null) {
            print(best.getAnswer());
        } else {
            print("Failed to find any match");
        }
    }

    /**
     * Pretty formatting for message block.
     *
     * @param strings The input strings.
     */
    public void print(List<String> strings) {
        System.out.println("\n\t" + strings.stream().map(str -> str.replace("\n", "\t\n")).collect(Collectors.joining()) + "\n");
    }

    /**
     * Pretty formatting for message block.
     *
     * @param strings The input strings.
     */
    public void print(String... strings) {
        System.out.println("\n\t" + Arrays.stream(strings).map(str -> str.replace("\n", "\t\n")).collect(Collectors.joining()) + "\n");
    }
}
