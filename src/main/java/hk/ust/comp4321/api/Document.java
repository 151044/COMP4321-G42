package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.TableOperation;
import hk.ust.comp4321.nlp.*;
import hk.ust.comp4321.util.StopWord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class representing a single document, indexed by its URL.
 *
 * <p> Note that the document is lazy - it does not actually load the words, their
 * associated frequencies, or children links unless {@link #retrieve()} or
 * {@link #retrieveFromDataBase(DatabaseConnection)} is called.
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
    public void retrieveFromDataBase(DatabaseConnection conn) throws SQLException {
        isLoaded = true;
    }

    /**
     * Retrieves the list of words in this document by connecting and parsing the webpage.
     * @throws IOException If connecting or reading from the URL fails
     */
    public void retrieve() throws IOException {
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
     * Since the document is lazy, only calls to {@link #retrieve()} or
     * {@link #retrieveFromDataBase(DatabaseConnection)} will set this to true.
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
