package hk.ust.comp4321.se;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;

import java.util.List;
import java.util.Map;

public class SearchEngine {

    DatabaseConnection conn;

    /***
     * Creates a new Search Engine.
     * @param conn The DatabaseConnection to use
     */
    public SearchEngine(DatabaseConnection conn) {
        this.conn = conn;
    }

    /***
     * Searches for 50 Documents related to query.
     * @param query The entire input query
     * @return List of Document-score pairs sorted by score in non-increasing order
     */
    public List<Map<Document, Double>> search(String query) {
        return search(query, 50);
    }

    /***
     * Searches for Documents related to query.
     * @param query The entire input query
     * @param numDocs The number of Documents to search for
     * @return List of Document-score pairs sorted by score in non-increasing order
     */
    public List<Map<Document, Double>> search(String query, int numDocs) {
        return List.of();
    }

}

