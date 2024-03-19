package hk.ust.comp4321.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StopWord {
    private static Set<String> stopWords;

    static {
        try {
            stopWords = new HashSet<>(Files.readAllLines(Path.of("data/nltk_stopwords.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isStopWord(String str) {
        return stopWords.contains(str);
    }



    private StopWord() {}
}
