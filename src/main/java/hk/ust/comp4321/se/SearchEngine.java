package hk.ust.comp4321.se;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordInfo;
import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.util.Tuple;

import java.util.*;

public class SearchEngine {

    private final DatabaseConnection conn;
    private final List<Document> docs;
    private final double TITLE_BOOST_FACTOR = 1.5;

    /***
     * Creates a new Search Engine.
     * @param conn The DatabaseConnection to use
     */
    public SearchEngine(DatabaseConnection conn, List<Document> docs) {
        this.conn = conn;
        this.docs = docs;
    }

    /***
     * Searches for Documents related to query.
     * @param query The entire input query
     * @return List of Document-score pairs sorted by score in non-increasing order
     */
    public List<Tuple<Document, Double>> search(SearchVector query) {
        return docs.stream()
                .map(d -> new Tuple<>(d, d.asTitleVector(conn).cosineSim(query) * TITLE_BOOST_FACTOR +
                        d.asBodyVector(conn).cosineSim(query)))
                .filter(d -> d.right() != 0.0)
                .sorted(Comparator.<Tuple<Document, Double>, Double>comparing(Tuple::right).reversed())
                .filter(d -> query.getRequiredTerms().stream()
                        .allMatch(s -> hasPhrase(d.left().bodyFrequencies(), s) || hasPhrase(d.left().titleFrequencies(), s)))
                .toList();
    }

    private boolean hasPhrase(Map<WordInfo, String> map, List<String> phrase) {
        if (!phrase.stream().allMatch(map::containsValue)) {
            return false;
        }
        List<Map.Entry<WordInfo, String>> l = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        int curParagraph = 0, phraseIdx = 0;
        for (Map.Entry<WordInfo, String> entry : l) {
            if (entry.getKey().paragraph() != curParagraph) {
                curParagraph = entry.getKey().paragraph();
                phraseIdx = 0;
            }
            if (entry.getValue().equals(phrase.get(phraseIdx))) {
                phraseIdx++;
            } else {
                phraseIdx = 0;
            }
            if (phraseIdx == phrase.size()) {
                return true;
            }
        }
        return false;
    }
}

