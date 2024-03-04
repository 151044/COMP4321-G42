package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordFrequency;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents the connection to the underlying database.
 *
 * <p>Currently, the database is organized into two different types of tables -
 * the Document table and Word (title) tables.
 *
 * <p>Please see
 * <a href="https://github.com/151044/COMP4321-G42/tree/main/docs/schema.md">
 *     the schema document
 * </a> for details.
 */
/*
 * Note to implementers: Please use PreparedStatements if your query
 * or statement is going to be run more than once, or has substitutable
 * variables. This prevents SQL injections and speeds up any future queries.
 *
 * See lab 1 for details, and further examples here.
 */
public class DatabaseConnection implements AutoCloseable {
    private final Connection conn;

    /**
     * Creates (if it does not exist) and connects to the database at the specified path.
     * @param path The path of the database to connect to
     * @throws SQLException If connecting or creating the database fails
     */
    public DatabaseConnection(Path path) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
        conn.setAutoCommit(false);
    }

    /**
     * Retrieves a document by its ID.
     *
     * <p>Note: The word frequencies of this document has not been loaded.
     *
     * @param docId The document ID to find a document for
     * @throws IllegalArgumentException If the document ID is invalid
     * @return The document associated with this ID.
     */
    public Document fromId(int docId) {
        return null;
    }

    /**
     * Inserts a word into the database.
     *
     * <p>The typical workflow is as follows:
     * <ol>
     *     <li>Check if the table corresponding to the word
     *          exists, and create it if needed.</li>
     *     <li>Update the table with the word frequency.</li>
     * </ol>
     * @param stem
     * @param freq
     */
    public void insertWord(String stem, WordFrequency freq) {

    }

    /**
     * Inserts a document into the database, or updates its last modified time
     * if it exists.
     *
     * <p>You do not need to create the document database since
     * it should be created on connection to the database.
     * @param doc
     */
    public void insertDocument(Document doc) {

    }

    /**
     * Commits all changes to the database which have occurred before the last call
     * to {@link #commit()}.
     * @throws SQLException If committing to the database throws an error
     */
    public void commit() throws SQLException {
        conn.commit();
    }

    /**
     * Gets the internal connection to the database.
     *
     * <p>This is intended for debugging - if you're using this it
     * probably means the API is not rich enough for your purposes.
     * Preferably, you should enhance this class instead.
     * @return The Connection object
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * {@inheritDoc}
     * @throws SQLException If closing the database throws an exception
     */
    @Override
    public void close() throws SQLException {
         conn.close();
    }
}
