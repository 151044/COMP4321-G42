package hk.ust.comp4321.se;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class representing a vector in the search engine.
 * A search vector can be generated for user queries and documents.
 */
public class SearchVector {
    private final Map<String, Double> vector = new HashMap<>();
    private final List<List<String>> requiredTerms = new ArrayList<>();

    /**
     * Constructs a new SearchVector with the terms and the weights specified.
     * @throws IllegalArgumentException If the length of the terms and the weights are mismatched
     * @param terms The list of terms in the search vector
     * @param weights The weight of each term in the search vector
     */
    public SearchVector(List<String> terms, List<Double> weights) {
        if (terms.size() != weights.size()) {
            throw new IllegalArgumentException("Term-weight length mismatch: The term length is %d while the weight length is %d"
                    .formatted(terms.size(), weights.size()));
        }
        for (int i = 0; i < terms.size(); i++) {
            vector.put(terms.get(i), weights.get(i));
        }
    }

    /**
     * Constructs a new SearchVector with the raw query from the search engine.
     * All weights are initialized to 1 in this scenario.
     * @param query The raw query string; May contain quotes
     */
    public SearchVector(String query) {
        String slice = query;
        List<String> quotes = new ArrayList<>();
        int i;
        int prev = -1;
        while ((i = slice.indexOf("\"", prev == -1 ? 0 : prev + 1)) != -1) {
            if (prev == -1) {
                prev = i;
            } else {
                quotes.add(slice.substring(prev + 1, i));
                slice = slice.substring(i + 1);
                prev = -1;
            }
        }
        requiredTerms.addAll(quotes.stream().map(s -> List.of(s.split(" "))).toList());
        List<String> terms = List.of(query.replace("\"", "").split(" "));
        terms.forEach(s -> vector.put(s, 1.0));
    }

    /**
     * Computes the cosine similarity between two search vectors.
     * @param other The other search vector to compute the distance between
     * @return The cosine similarity between the two vectors; or 0 if they share nothing in common
     */
    public double cosineSim(SearchVector other) {
        Set<String> keys = new HashSet<>(vector.keySet());
        keys.retainAll(other.vector.keySet());
        if (keys.isEmpty()) {
            return 0;
        }
        double inner = keys.stream().mapToDouble(k -> vector.get(k) * other.vector.get(k)).sum();
        double docLen = Math.sqrt(vector.values().stream().mapToDouble(d -> d * d).sum());
        double queryLen = Math.sqrt(other.vector.values().stream().mapToDouble(d -> d * d).sum());
        return inner / (docLen * queryLen);
    }

    /**
     * Gets the list of quoted terms which must exist in the document.
     * For example, the method returns the list [a, b] for the query {"a b" c d}.
     * @return The list of quoted terms, tokenized
     */
    public List<List<String>> getRequiredTerms() {
        return requiredTerms;
    }

    @Override
    public String toString() {
        return vector.entrySet().stream().map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining(", "));
    }
}
