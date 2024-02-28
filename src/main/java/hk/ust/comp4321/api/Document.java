package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A class representing a single document, indexed by its URL.
 * @param url The URL of the document
 * @param lastModified The timestamp at which the document was last modified
 * @param id The document ID; must be unique
 */
public record Document(URL url, LocalDateTime lastModified, long id) {
    /**
     * Retrieves the list of words in this document from the database.
     * @param conn The database connection to use
     * @throws SQLException If there is an SQL error
     * @return The list of words in this document
     */
    public List<String> retrieve(DatabaseConnection conn) throws SQLException {
        return null;
    }

    /**
     * Retrieves the list of words in this document by connecting and parsing the webpage.
     * @param soup The JSoup instance to use
     * @return The list of words retrieved from the URL
     * @throws IOException If connecting or reading from the URL fails
     */
    public List<String> retrieve(Jsoup soup) throws IOException {
        return null;
    }

    /**
     * Writes the updated list of words to the database.
     * @param conn The database connection to use
     */
    public void write(DatabaseConnection conn) {

    }
}
