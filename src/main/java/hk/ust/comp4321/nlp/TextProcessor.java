package hk.ust.comp4321.nlp;

import hk.ust.comp4321.util.ResourceLoader;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Class wrapping the tokenizer and sentence detector from OpenNLP.
 */
public class TextProcessor {
    private final SentenceDetectorME sentenceModel;
    private final TokenizerME tokenModel;

    private static final TextProcessor INSTANCE;
    static {
        try {
            INSTANCE = new TextProcessor(
                    ResourceLoader.loadResource(Path.of("data/sentence-model-en.bin")),
                    ResourceLoader.loadResource(Path.of("data/token-model-en.bin")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new TextProcessor with the specified models.
     * @param sentenceStream The InputStream to read the sentence detector model from
     * @param tokenStream The InputStream to read the tokenizer model from
     * @throws IOException If the models cannot be found or read
     */
    public TextProcessor(InputStream sentenceStream, InputStream tokenStream) throws IOException {
        sentenceModel = new SentenceDetectorME(new SentenceModel(sentenceStream));
        tokenModel = new TokenizerME(new TokenizerModel(tokenStream));
    }

    /**
     * Converts a paragraph (or a series of sentences) into a list of sentences.
     *
     * <p>The tokenizer considers punctuation as individual tokens, but hyphenated
     * words are considered a single token.
     * @param text The paragraph to parse into sentences
     * @return A List of Strings where each element represents a sentence
     */
    public List<String> toSentence(String text) {
        return List.of(sentenceModel.sentDetect(text));
    }

    /**
     * Converts a sentence into a list of words (tokens).
     * @param text The sentence to parse into tokens
     * @return A List of Strings where each element represents a word (a token)
     */
    public List<String> toTokens(String text) {
        return List.of(tokenModel.tokenize(text));
    }

    /**
     * Gets the text processor instance with the default models.
     * @return The default text processor
     */
    public static TextProcessor getInstance() {
        return INSTANCE;
    }

    public static boolean allSymbols(String text) {
        List<Character> characters = text.chars().mapToObj(c -> (char)c).toList();
        for (char ch: characters) {
            if (Character.isDigit(ch) || Character.isLetter(ch)) {
                return false;
            }
        }
        return true;
    }
}
