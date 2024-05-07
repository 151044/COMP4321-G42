package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.TableOperation;
import hk.ust.comp4321.nlp.*;
import hk.ust.comp4321.se.SearchVector;
import hk.ust.comp4321.util.StopWord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final Map<WordInfo, String> bodyFrequencies = new HashMap<>();
    private final Map<WordInfo, String> titleFrequencies = new HashMap<>();
    private final List<URL> children = new ArrayList<>();
    private boolean isLoaded = false;
    private String title = "";
    private SearchVector titleVector;
    private SearchVector bodyVector;

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
     * Creates a new Document with the specified URL and title.
     *
     * @implNote This should be called from the database and not by user code.
     * @param url The URL of the document
     * @param lastModified The Unix timestamp at which the document was last modified
     * @param id The document ID; must be unique
     * @param size The number of words of the document
     * @param title The title of the page
     */
    public Document(URL url, int id, Instant lastModified, long size, String title) {
        this.url = url;
        this.lastModified = lastModified;
        this.id = id;
        this.size = size;
        this.title = title;
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
        List<Integer> titleStemIds = titleTable.getStemIds(id);
        for (int stemId: titleStemIds) {
            List<WordInfo> titleInfoList = titleTable.getFrequency(stemId, id);
            String stem = titleTable.getStemFromId(stemId);
            for (WordInfo wordInfo: titleInfoList) {
                this.titleFrequencies.put(wordInfo, stem);
            }
        }

        // Load bodyFrequencies
        TableOperation bodyTable = conn.bodyOperator();
        List<Integer> bodyStemIds = bodyTable.getStemIds(id);
        for (int stemId: bodyStemIds) {
            List<WordInfo> bodyInfoList = bodyTable.getFrequency(stemId, id);
            String stem = bodyTable.getStemFromId(stemId);
            for (WordInfo wordInfo: bodyInfoList) {
                this.bodyFrequencies.put(wordInfo, stem);
            }
        }

        // Load child documents
        this.children.addAll(conn.children(this.id).stream().toList());

        // Document is completely loaded
        this.isLoaded = true;
    }

    /**
     * Retrieves the list of words in this document from the parsed Jsoup Document, which is already retrieved by
     * the spider successfully using a GET request.
     * @param jsoupDoc The webpage in Jsoup document form
     */
    public void retrieveFromWeb(org.jsoup.nodes.Document jsoupDoc) {
        // Load text processor
        TextProcessor textProcessor = TextProcessor.getInstance();

        // Extract title sections
        // Note: There must be exactly one title per HTMl file (i.e., one "paragraph" only)
        title = jsoupDoc.select("title").text();
        List<String> titleSentences = textProcessor.toSentence(title);
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
                    // Store empty string is the stemmed word is identical to the raw word
                    if (stemmedWord.equals(rawWord)) {
                        this.titleFrequencies.put(new WordInfo(this.id, 0, j, k, ""), stemmedWord);
                    } else {
                        this.titleFrequencies.put(new WordInfo(this.id, 0, j, k, rawWord), stemmedWord);
                    }
                }
            }
        }

        // Extract body sections
        Elements body = jsoupDoc.select("body");

        // Note: A site can be empty
        if (!body.isEmpty()) {
            List<String> bodySections = body.get(0).textNodes().stream().map(TextNode::text).toList();
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
                                this.bodyFrequencies.put(new WordInfo(this.id, i, j, k, ""), stemmedWord);
                            } else {
                                this.bodyFrequencies.put(new WordInfo(this.id, i, j, k, rawWord), stemmedWord);
                            }
                        }
                    }
                }
            }
        }

        // Extract links
        Elements links = jsoupDoc.select("a[href]");
        for (Element link : links) {
            try {
                this.children.add(URI.create(link.attr("abs:href")).toURL());
            } catch (IllegalArgumentException | MalformedURLException ex) {
//                System.out.println("Error occurred when crawling this page: " + link.attr("abs:href") + " and hence skipped");
            }
        }

        // Document is completely loaded
        isLoaded = true;
    }

    /**
     * Retrieves the list of words in this document by connecting and parsing the webpage.
     * @throws IOException If connecting or reading from the URL fails
     * <p> Note that this method is only used for testing - instead of this method, the spider should get
     * the Jsoup document and pass it to {@link #retrieveFromWeb(org.jsoup.nodes.Document)},
     * given that the GET request is successful.
     */
    public void retrieveFromWeb() throws IOException {
        // Connect to the URL
        org.jsoup.Connection docConnection = Jsoup.connect(this.url.toString());

        // Try to execute the request
        // If IOException is thrown, the document remains unloaded and exit the method immediately
        try {
            docConnection.execute();
        } catch (IOException ex) {
            return;
        }

        retrieveFromWeb(docConnection.get());
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
        for (WordInfo titleWordInfo: titleFrequencies.keySet()) {
            int stemId = titleTable.insertStem(titleFrequencies.get(titleWordInfo));
            titleTable.insertWordInfo(stemId, titleWordInfo);
        }
        for (WordInfo bodyWordInfo: bodyFrequencies.keySet()) {
            int stemId = bodyTable.insertStem(bodyFrequencies.get(bodyWordInfo));
            bodyTable.insertWordInfo(stemId, bodyWordInfo);
        }
    }

    /**
     * Writes the child links scraped to the database as URLs.
     *
     * @param conn The database connection to use
     */
    public void writeChildrenLinks(DatabaseConnection conn) {
        // For each child links, find its corresponding child document and extract its document ID and then insert link to the database
        this.children.forEach(u -> conn.insertLink(this.id, u));
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
    public Map<WordInfo, String> bodyFrequencies() {
        return bodyFrequencies;
    }

    /**
     * Gets the current list of words in the document title and their associated frequencies.
     * If {@link #isLoaded()} returns false, this returns an empty map instead.
     * @return The list of words loaded, or an empty map if
     * the words have not been loaded into memory.
     */
    public Map<WordInfo, String> titleFrequencies() {
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

    /**
     * Gets the title of the document, or an empty String if the document is not loaded.
     * @return The text in the title tag of the document
     */
    public String title() {
        return title;
    }

    /**
     * Converts the titles of this document into a search query.
     * @param documents The list of all documents
     * @return The search vector corresponding to the titles in this document
     */
    public SearchVector asTitleVector(List<Document> documents) {
        if (titleVector == null) {
            titleVector = termWeights(titleFrequencies, documents, d -> d.titleFrequencies);
        }
        return titleVector;
    }

    /**
     * Converts the body of this document into a search query.
     * @param documents The list of all documents
     * @return The search vector corresponding to the body in this document
     */
    public SearchVector asBodyVector(List<Document> documents) {
        if (bodyVector == null) {
            bodyVector = termWeights(bodyFrequencies, documents, d -> d.bodyFrequencies);
        }
        return bodyVector;
    }

    private SearchVector termWeights(Map<WordInfo, String> info, List<Document> docs, Function<Document, Map<WordInfo, String>> converter) {
        Map<String, Long> values = info.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long maxTerm = values.values().stream().max(Long::compare).orElse(0L);
        if (maxTerm == 0) {
            return new SearchVector(List.of(), List.of());
        }
        List<Map.Entry<String, Long>> l = values.entrySet().stream().toList();
        return new SearchVector(l.stream().map(Map.Entry::getKey).toList(),
                l.stream().map(e -> e.getValue() * (Math.log((double) DatabaseConnection.getDocSize() /
                        docs.parallelStream().map(converter)
                                .filter(m -> m.containsValue(e.getKey())).count()) / Math.log(2)) / maxTerm).toList());
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
