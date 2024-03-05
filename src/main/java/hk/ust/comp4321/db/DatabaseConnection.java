package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordFrequency;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Represents the connection to the underlying database.
 *
 * <p>Currently, the database is organized into three different types of tables -
 * the Document table, the Document Link table and Word (title) tables.
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
 *
 * Note: you don't need to specify text length in SQLite, so varchar without the brackets
 * is fine.
 */
public class DatabaseConnection implements AutoCloseable {
    private final Connection conn;

    /**
     * Creates (if it does not exist) and connects to the database at the specified path.
     *
     * <p>Note that automatic commits have been disabled for performance reasons. Please
     * remember to call {@link #commit()} for each batch of writes to the database.
     * @param path The path of the database to connect to
     * @throws SQLException If connecting or creating the database fails
     */
    public DatabaseConnection(Path path) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
        conn.setAutoCommit(false);
        Statement createTable = conn.createStatement();
        createTable.execute("CREATE TABLE IF NOT EXISTS Document " +
                "(URL varchar, DocId integer, LastModified integer, Size integer)");
        createTable.execute("CREATE TABLE IF NOT EXISTS DocumentLink" +
                "(DocId integer, ChildId integer)");
    }

    /**
     * Retrieves a document by its ID.
     *
     * <p>Note: The word frequencies and children of this document have not been loaded.
     *
     * @param docId The document ID to find a document for
     * @throws IllegalArgumentException If the document ID is invalid
     * @return The document associated with this ID.
     */
    public Document getDocFromId(int docId) {
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
     * @param stem The stemmed word to insert
     * @param freq The word frequency record to associate with this word
     */
    public void insertWord(String stem, WordFrequency freq) {

    }

    /**
     * Inserts a word into the corresponding title table of the database.
     *
     * <p>The typical workflow is as follows:
     * <ol>
     *     <li>Check if the title table corresponding to the word
     *          exists, and create it if needed.</li>
     *     <li>Update the table with the word frequency.</li>
     * </ol>
     * @param stem The stemmed word to insert
     * @param freq The word frequency record to associate with this word
     */
    public void insertTitleWord(String stem, WordFrequency freq) {

    }

    /**
     * Inserts a document into the database, or updates its last modified time
     * if it exists.
     *
     * <p>You do not need to create the document database since
     * it should be created on connection to the database.
     *
     * <p>This does not insert the document's words into the database.
     * @param doc The document to insert into
     */
    public void insertDocument(Document doc) {

    }

    /**
     * Finds the corresponding word frequencies of the stem.
     * This <strong>does not</strong> retrieve the title frequency.
     * @param stem The stem to find the word frequencies for
     * @return The list of word frequencies associated with this stem
     */
    public List<WordFrequency> frequenciesOf(String stem) {
       return List.of();
    }

    /**
     * Finds the corresponding title word frequencies of the stem.
     * This <strong>does not</strong> retrieve the word frequency in the document body.
     * @param stem The stem to find the word frequencies for
     * @return The list of word frequencies associated with this stem
     */
    public List<WordFrequency> frequenciesOfTitle(String stem) {
        return List.of();
    }

    /**
     * Inserts a link into the document link database.
     * @param docId The parent document id
     * @param child The child document id
     */
    public void insertLink(int docId, int child) {

    }

    /**
     * Drops all the word frequency records associated with this document ID.
     * @param docId The document ID to purge frequencies for
     */
    public void deleteFrequencies(int docId) {

    }

    /**
     * Drops all the "forward links" associated with this document ID.
     * This means that all records in the document link table with the
     * specified DocId field should be dropped.
     * @param docId The document ID to purge links for
     */
    public void deleteForwardLinks(int docId) {

    }

    /**
     * Commits all changes to the database which have occurred before the last call
     * to this method.
     * @throws SQLException If committing to the database throws an error
     */
    public void commit() throws SQLException {
        conn.commit();
    }

    /**
     * Gets the next document ID.
     *
     * <p>Possible implementations include counting the columns and reading the columns
     * until you find the next available ID.
     * @return The next unique document ID
     */
    public int nextDocId() {
        return 0;
    }

    /**
     * Gets the internal connection to the database.
     *
     * <p>This is intended for debugging - if you're using this it
     * probably means the API is not rich enough for your purposes.
     * Preferably, you should enhance this class by adding more methods
     * in this class instead.
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
