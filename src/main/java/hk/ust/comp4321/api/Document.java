package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A class representing a single document, indexed by its URL.
 * <p>
 * Note that the document is lazy - it does not actually load the words unless
 * {@link #retrieve(Jsoup)} or {@link #retrieve(DatabaseConnection)} is called.
 * </p>
 */
public final class Document {
    private final URL url;
    private final LocalDateTime lastModified;
    private final long id;
    private final List<String> keywords = new ArrayList<>();
    private boolean isLoaded = false;

    /**
     * @param url The URL of the document
     * @param lastModified The timestamp at which the document was last modified
     * @param id The document ID; must be unique
     */
    public Document(URL url, LocalDateTime lastModified, long id) {
        this.url = url;
        this.lastModified = lastModified;
        this.id = id;
    }

    /**
     * Retrieves the list of words in this document from the database.
     * @param conn The database connection to use
     * @return The list of words in this document
     * @throws SQLException If there is an SQL error
     */
    public List<String> retrieve(DatabaseConnection conn) throws SQLException {
        isLoaded = true;
        return null;
    }

    /**
     * Retrieves the list of words in this document by connecting and parsing the webpage.
     * @param soup The JSoup instance to use
     * @return The list of words retrieved from the URL
     * @throws IOException If connecting or reading from the URL fails
     */
    public List<String> retrieve(Jsoup soup) throws IOException {
        isLoaded = true;
        return null;
    }

    /**
     * Writes the updated list of words to the database.
     * @param conn The database connection to use
     */
    public void write(DatabaseConnection conn) {

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
     * @return The local date time at which this document is modified
     */
    public LocalDateTime lastModified() {
        return lastModified;
    }

    /**
     * Gets the document ID.
     * @return The ID of this document
     */
    public long id() {
        return id;
    }
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Gets the current list of loaded words.
     * @return The list of words to load
     */
    public List<String> currentWords() {
        return keywords;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Document) obj;
        return Objects.equals(this.url, that.url) &&
                Objects.equals(this.lastModified, that.lastModified) &&
                this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, lastModified, id);
    }

    @Override
    public String toString() {
        return "Document[" + url + ", " + id + "]";
    }

}
