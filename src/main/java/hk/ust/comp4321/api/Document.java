package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.TableOperation;
import hk.ust.comp4321.nlp.*;
import hk.ust.comp4321.util.StopWord;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

/**
 * A class representing a single document, indexed by its URL.
 *
 * <p> Note that the document is lazy - it does not actually load the words, their
 * associated frequencies, or children links unless {@link #retrieveFromWeb()} or
 * {@link #retrieveFromDatabase(DatabaseConnection)} is called.
 */
public final class Document {
    private final URL url;
    private final Instant lastModified;
    private final int id;
    private final long size;
    private final Map<String, WordInfo> bodyFrequencies = new HashMap<>();
    private final Map<String, WordInfo> titleFrequencies = new HashMap<>();
    private final List<URL> children = new ArrayList<>();
    private boolean isLoaded = false;

    /**
     * Creates a new Document with the specified URL.
     * @param url The URL of the document
     * @param lastModified The Unix timestamp at which the document was last modified
     * @param id The document ID; must be unique
     * @param size The number of words of the document
     */
    public Document(URL url, int id, Instant lastModified, long size) {
        this.url = url;
        this.lastModified = lastModified;
        this.id = id;
        this.size = size;
    }

    /**
     * Retrieves the list of words in this document from the database.
     *
     * <p>Note that this call is very computationally expensive - the database
     * design makes retrieving a list of all words very slow. Effectively, the
     * entire database needs to be traversed in order to build this list.
     * Please use this method sparingly.
     * @param conn The database connection to use
     * @throws SQLException If there is an SQL error
     */
    public void retrieveFromDatabase(DatabaseConnection conn) throws SQLException {
        // Load titleFrequencies
        TableOperation titleTable = conn.titleOperator();
        List<Integer> titleStemIds = titleTable.getStemIds();

        for (int stemId: titleStemIds) {
            List<WordInfo> titleInfoList = titleTable.getFrequency(stemId).stream().filter(x -> x.docId() == this.id).toList();
            String stem = titleTable.getStemFromId(stemId);
            for (WordInfo wordInfo: titleInfoList) {
                this.titleFrequencies.put(stem, wordInfo);
            }
        }

        // Load bodyFrequencies
        TableOperation bodyTable = conn.bodyOperator();
        List<Integer> bodyStemIds = bodyTable.getStemIds();
        for (int stemId: bodyStemIds) {
            List<WordInfo> bodyInfoList = bodyTable.getFrequency(stemId).stream().filter(x -> x.docId() == this.id).toList();
            String stem = bodyTable.getStemFromId(stemId);
            for (WordInfo wordInfo: bodyInfoList) {
                this.bodyFrequencies.put(stem, wordInfo);
            }
        }

        // Load child documents
        this.children.addAll(conn.children(this.id).stream().map(Document::url).toList());

        // Document is completely loaded
        this.isLoaded = true;
    }

    /**
     * Retrieves the list of words in this document by connecting and parsing the webpage.
     * @throws IOException If connecting or reading from the URL fails
     */
    public void retrieveFromWeb() throws IOException {
        // Connect to the URL
        org.jsoup.nodes.Document doc = Jsoup.connect(this.url.toString()).get();

        // Load text processor
        TextProcessor textProcessor = TextProcessor.getInstance();

        // Extract title sections
        // Note: There can be only one title per HTMl file (i.e., one "paragraph" only)
        List<String> titleSentences = textProcessor.toSentence(doc.select("title").text());
        // Sentence level
        for (int j = 0; j < titleSentences.size(); ++j) {
            /*
            For every sentence:
            1. Tokenize the sentence
            2. Filter out tokens that are all symbols
            3. Turn all the tokens into lowercase
             */
            List<String> rawTitleWords = textProcessor.toTokens(titleSentences.get(j))
                    .stream()
                    .filter(text -> !TextProcessor.isAllSymbols(text))
                    .map(String::toLowerCase).toList();
            // Word level
            for (int k = 0; k < rawTitleWords.size(); ++k) {
                String rawWord = rawTitleWords.get(k);
                // Put the stem to the map if it is not a stop word
                if (!StopWord.isStopWord(rawWord)) {
                    String stemmedWord = NltkPorter.stem(rawWord);
                    if (stemmedWord.equals(rawWord)) {
                        this.titleFrequencies.put(stemmedWord, new WordInfo(this.id, 0, j, k, ""));
                    } else {
                        this.titleFrequencies.put(stemmedWord, new WordInfo(this.id, 0, j, k, rawWord));
                    }
                }
            }
        }

        // Extract body sections
        List<String> bodySections = doc.select("body").get(0).children().stream().map(Element::text).toList();
        // Paragraph level
        for (int i = 0; i < bodySections.size(); ++i) {
            List<String> bodySentences = textProcessor.toSentence(bodySections.get(i));
            // Sentence level
            for (int j = 0; j < bodySentences.size(); ++j) {
                List<String> rawBodyWords = textProcessor.toTokens(bodySentences.get(j))
                        .stream()
                        .filter(text -> !TextProcessor.isAllSymbols(text))
                        .map(String::toLowerCase).toList();
                // Word level
                for (int k = 0; k < rawBodyWords.size(); ++k) {
                    String rawWord = rawBodyWords.get(k);
                    if (!StopWord.isStopWord(rawWord)) {
                        String stemmedWord = NltkPorter.stem(rawWord);
                        if (stemmedWord.equals(rawWord)) {
                            this.bodyFrequencies.put(stemmedWord, new WordInfo(this.id, i, j, k, ""));
                        } else {
                            this.bodyFrequencies.put(stemmedWord, new WordInfo(this.id, i, j, k, rawWord));
                        }
                    }
                }
            }
        }

        // Extract links
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            this.children.add(new URL(link.attr("abs:href")));
        }

        // Document is completely loaded
        isLoaded = true;
    }

    /**
     * Writes the updated list of words to the database.
     *
     * <p><strong>Do not</strong> write the links to the database here.
     * @param conn The database connection to use
     */
    public void writeWords(DatabaseConnection conn) {
        // Insert the document into the database
        conn.insertDocument(this);

        // Get access to title table and body table
        TableOperation titleTable = conn.titleOperator();
        TableOperation bodyTable = conn.bodyOperator();

        // For every pair of stem (String) and frequency (WordInfo), get a stem ID and insert the ID and the frequency into the database
        for (String titleStem: titleFrequencies.keySet()) {
            int stemId = titleTable.insertStem(titleStem);
            titleTable.insertWordInfo(stemId, titleFrequencies.get(titleStem));
        }
        for (String bodyStem: bodyFrequencies.keySet()) {
            int stemId = bodyTable.insertStem(bodyStem);
            bodyTable.insertWordInfo(stemId, bodyFrequencies.get(bodyStem));
        }
    }

    /**
     * Writes the child links scraped to the database as document IDs.
     *
     * <p>This can <strong>only</strong> be called after all the documents
     * currently discovered have been written to the database. Otherwise,
     * we cannot resolve the URLs into DocIds properly.
     * @param conn The database connection to use
     */
    public void writeChildrenLinks(DatabaseConnection conn) {
        // For each child links, find its corresponding child document and extract its document ID and then insert link to the database
        this.children.forEach(u -> conn.insertLink(this.id, conn.getDocFromUrl(u).id()));
    }

    /**
     * Gets the URL representing this document.
     * @return The URL of this document
     */
    public URL url() {
        return url;
    }

    /**
     * Gets the time when the document is last modified.
     * @return The Instant at which this document is modified
     */
    public Instant lastModified() {
        return lastModified;
    }

    /**
     * Gets the document ID.
     * @return The ID of this document
     */
    public int id() {
        return id;
    }

    /**
     * Checks if the list of words of this document are loaded.
     * Since the document is lazy, only calls to {@link #retrieveFromWeb()} or
     * {@link #retrieveFromDatabase(DatabaseConnection)} will set this to true.
     * @return True if the list of words are loaded, false otherwise
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Gets the current list of words in the document body and their associated frequencies.
     * If {@link #isLoaded()} returns false, this returns an empty map instead.
     * @return The list of words loaded, or an empty map if
     * the words have not been loaded into memory.
     */
    public Map<String, WordInfo> bodyFrequencies() {
        return bodyFrequencies;
    }

    /**
     * Gets the current list of words in the document title and their associated frequencies.
     * If {@link #isLoaded()} returns false, this returns an empty map instead.
     * @return The list of words loaded, or an empty map if
     * the words have not been loaded into memory.
     */
    public Map<String, WordInfo> titleFrequencies() {
        return titleFrequencies;
    }

    /**
     * Gets the documents discovered in this document.
     * If {@link #isLoaded()} returns false, this returns an empty list instead.
     * @return The list of discovered URLs, or an empty list if the document is not loaded
     */
    public List<URL> children() {
        return children;
    }

    /**
     * Gets the size of the document, which is the number of words in the body.
     * @return The size of the document
     */
    public long size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Document) obj;
        return Objects.equals(this.url, that.url) &&
                this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, id);
    }

    @Override
    public String toString() {
        return "Document[" + url + ", " + id + "]";
    }
}
