package hk.ust.comp4321.se;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.util.Tuple;

import java.util.Comparator;
import java.util.List;

public class SearchEngine {

    private DatabaseConnection conn;
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
     * Searches for 50 Documents related to query.
     * @param query The entire input query
     * @return List of Document-score pairs sorted by score in non-increasing order
     */
    public List<Tuple<Document, Double>> search(SearchVector query) {
        return search(query, 50);
    }

    /***
     * Searches for Documents related to query.
     * @param query The entire input query
     * @param numDocs The number of Documents to search for
     * @return List of Document-score pairs sorted by score in non-increasing order
     */
    public List<Tuple<Document, Double>> search(SearchVector query, int numDocs) {
        return docs.stream()
                .map(d -> new Tuple<>(d, d.asTitleVector(conn).cosineSim(query) * TITLE_BOOST_FACTOR +
                        d.asBodyVector(conn).cosineSim(query)))
                .sorted(Comparator.<Tuple<Document, Double>, Double>comparing(Tuple::right).reversed())
                .limit(numDocs)
                .toList();
    }

}

