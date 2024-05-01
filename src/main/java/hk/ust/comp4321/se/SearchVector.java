package hk.ust.comp4321.se;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class representing a vector in the search engine.
 * A search vector can be generated for user queries and documents.
 */
public class SearchVector {
    private final Map<String, Double> vector;

    /**
     * Constructs a new SearchVector with the terms and the weights specified.
     * @throws IllegalArgumentException If the length of the terms and the weights are mismatched
     * @param terms The list of terms in the search vector
     * @param weights The weight of each term in the search vector
     */
    public SearchVector(List<String> terms, List<Double> weights) {
        vector = new HashMap<>();
        if (terms.size() != weights.size()) {
            throw new IllegalArgumentException("Term-weight length mismatch: The term length is %d while the weight length is %d"
                    .formatted(terms.size(), weights.size()));
        }
    }

    /**
     * Computes the cosine similarity between two search vectors.
     * @param other The other search vector to compute the distance between
     * @return The cosine similarity between the two vectors; or 0 if they share nothing in common
     */
    public double cosineSim(SearchVector other) {
        Set<String> keys = vector.keySet();
        keys.retainAll(other.vector.keySet());
        double inner = keys.stream().mapToDouble(k -> vector.get(k) * other.vector.get(k)).sum();
        double docLen = Math.sqrt(vector.values().stream().mapToDouble(d -> d * d).sum());
        double queryLen = Math.sqrt(other.vector.values().stream().mapToDouble(d -> d * d).sum());
        return inner / (docLen * queryLen);
    }
}
