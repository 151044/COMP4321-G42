package hk.ust.comp4321.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to represent the list of stopwords.
 */
public class StopWord {
    private static final Set<String> stopWords;

    static {
        try {
            stopWords = new BufferedReader(new InputStreamReader(
                    ResourceLoader.loadResource(Path.of("data/nltk_stopwords.txt"))))
                    .lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a word is in the stop word list.
     * @param str The string to check
     * @return True if the word is a stopword; false otherwise
     */
    public static boolean isStopWord(String str) {
        return stopWords.contains(str);
    }

    /**
     * Removes all stop words from a specified string.
     * @param words The string to strip the stop words from
     * @return The original string with all stopwords removed
     */
    public static String stripStopwords(String words) {
        List<String> tokenized = new ArrayList<>(List.of(words.split(" ")));
        StringBuilder sb = new StringBuilder(" ");
        for (int i = 0; i < tokenized.size(); i++) {
            String stripped = tokenized.get(i).replace("\"", "");
            if (stopWords.contains(stripped)) {
                sb.append(tokenized.get(i).contains("\"") ? "\"" : "");
            } else {
                sb.append(" ").append(tokenized.get(i));
            }
        }
        return sb.toString().replace(" \" ", " \"").strip();
    }

    private StopWord() {}
}
