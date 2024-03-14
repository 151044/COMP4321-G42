package hk.ust.comp4321.api;

/**
 * Record representing the frequency and location of a word in a given document.
 * @param docId The document ID this word frequency record belongs to
 * @param paragraph The paragraph number of this word
 * @param sentence The sentence number of this word
 * @param wordLocation The location of the word is in the sentence
 * @param suffix The suffix of the stemmed word. Combining the stemmed word with the suffix should produce the original word.
 */
public record WordInfo(int docId, int paragraph, int sentence, int wordLocation, String suffix) {}
