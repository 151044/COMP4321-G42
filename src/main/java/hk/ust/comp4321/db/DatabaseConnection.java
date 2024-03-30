package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jooq.impl.SQLDataType.*;

/**
 * Represents the connection to the underlying database.
 *
 * <p>Currently, the database is organized into four different types of tables -
 * the Document table, the Document Link table, the Word Index table and Word (title) tables.
 *
 * <p>Please see
 * <a href="https://github.com/151044/COMP4321-G42/tree/main/docs/schema.md">
 *     the schema document
 * </a> for details.
 */
/*
 * Note to implementers: We have switched to jOOQ in order to have a
 * nice OO wrapper around SQL. Much of normal SQL still applies in varying ways.
 * If you need to implement something, please see the current implemented examples.
 *
 * The jOOQ documentation is here: https://www.jooq.org/doc/3.19/manual/, though it
 * is a very dense read. You might have better luck finding the equivalent SQL then
 * Googling the correct syntax.
 */
public class DatabaseConnection implements AutoCloseable {
    private final Connection conn;
    private static AtomicInteger nextDocId = null;
    private final DSLContext create;

    /**
     * Creates (if it does not exist) and connects to the database at the specified path.
     *
     * @param path The path of the database to connect to
     * @throws SQLException If connecting or creating the database fails
     */
    public DatabaseConnection(Path path) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
        create = DSL.using(conn, SQLDialect.SQLITE);
        create.execute("PRAGMA foreign_keys = TRUE");
        create.execute("PRAGMA journal_mode = WAL;");
        create.execute("PRAGMA synchronous = NORMAL");
        create.createTableIfNotExists("Document")
                .column("url", VARCHAR)
                .column("docId", INTEGER)
                .column("lastModified", INSTANT)
                .column("size", BIGINT)
                .column("title", VARCHAR)
                .constraint(
                        DSL.primaryKey("docId")
                ).execute();
        create.createTableIfNotExists("DocumentLink")
                .column("docId", INTEGER)
                .column("childUrl", VARCHAR)
                .constraints(
                        DSL.primaryKey("docId", "childUrl"),
                        DSL.foreignKey("docId").references("Document", "docId")
                )
                .execute();

        create.createTableIfNotExists("WordIndex")
                .column("stem", VARCHAR)
                .column("wordId", INTEGER)
                .column("typePrefix", VARCHAR)
                .constraints(
                        DSL.primaryKey("wordId", "typePrefix")
                )
                .execute();

        create.createTableIfNotExists("ForwardIndex")
                .column("docId", INTEGER)
                .column("wordId", INTEGER)
                .column("typePrefix", VARCHAR)
                .constraints(
                        DSL.primaryKey("docId", "wordId", "typePrefix"),
                        DSL.foreignKey("docId").references("Document", "docId")
                )
                .execute();

        if (nextDocId == null) {
            nextDocId = new AtomicInteger(create.fetchCount(DSL.table("Document")));
        }
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
        return create.select()
                .from(DSL.table("Document"))
                .where(
                        DSL.condition(DSL.field(DSL.name("docId")).eq(docId))
                ).fetch().stream().findFirst()
                .map(r -> {
                    try {
                        return new Document(new URL(r.get(0, String.class)),
                                r.get(1, Integer.class),
                                r.get(2, Instant.class),
                                r.get(3, Long.class),
                                r.get(4, String.class));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("No such document ID: " + docId));
    }

    /**
     * Checks if a document ID exists in the database.
     * @param docId The document ID to verify the existence of
     * @return True if the document ID exists in the database; false otherwise
     */
    public boolean hasDocId(int docId) {
        return create.fetchCount(DSL.table("Document"), DSL.condition(DSL.field(DSL.name("docId")).eq(docId))) > 0;
    }

    /**
     * Retrieves a document by its URL.
     *
     * <p>Note: The word frequencies and children of this document have not been loaded.
     *
     * @param url The corresponding URL to find a document for
     * @throws IllegalArgumentException If the URL is invalid
     * @return The document associated with this URL.
     */
    public Document getDocFromUrl(URL url) {
        return create.select()
                .from(DSL.table("Document"))
                .where(
                        DSL.condition(DSL.field(DSL.name("url")).eq(url.toString()))
                ).fetch().stream().findFirst()
                .map(r -> {
                    try {
                        return new Document(new URL(r.get(0, String.class)),
                                r.get(1, Integer.class),
                                r.get(2, Instant.class),
                                r.get(3, Long.class),
                                r.get(4, String.class));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("No such URL: " + url));
    }

    /**
     * Checks if a document exists in the database by its URL.
     * @param url The document with corresponding URL to verify the existence of
     * @return True if the document exists in the database; false otherwise
     */
    public boolean hasDocUrl(URL url) {
        return create.fetchCount(DSL.table("Document"), DSL.condition(DSL.field(DSL.name("url")).eq(url.toString()))) > 0;
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
        create.insertInto(DSL.table("Document"))
                .values(doc.url().toString(), doc.id(), doc.lastModified(), doc.size(), doc.title())
                .onDuplicateKeyUpdate()
                .set(DSL.field("lastModified", INSTANT), doc.lastModified())
                .set(DSL.field("size", BIGINT), doc.size())
                .set(DSL.field("title", VARCHAR), doc.title())
                .execute();
    }

    /**
     * Retrieves all the entries in the document table.
     * @return The list of all tables
     */
    public List<Document> getDocuments() {
        return create.select()
                .from(DSL.table("Document"))
                .fetch().stream()
                .map(r -> {
                    try {
                        return new Document(new URL(r.get(0, String.class)),
                                r.get(1, Integer.class),
                                r.get(2, Instant.class),
                                r.get(3, Long.class),
                                r.get(4, String.class));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    /**
     * Inserts a link into the document link database.
     * @param docId The parent document ID
     * @param child The child URL
     */
    public void insertLink(int docId, URL child) {
        create.insertInto(DSL.table("DocumentLink"))
                .values(docId, child.toString())
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Retrieves the list of children URLs for the specified document ID.
     *
     * @param docId The document ID to retrieve the children for
     * @return A list of child URLs for the specified document ID
     */
    public List<URL> children(int docId) {
        return create.select(DSL.field(DSL.name("childUrl"))).from(DSL.table(DSL.name("DocumentLink")))
                .where(DSL.condition(DSL.field(DSL.name("docId")).eq(docId)))
                .fetch().map(r -> {
                    try {
                        return new URI(r.get(0, String.class)).toURL();
                    } catch (MalformedURLException | URISyntaxException e) {
                        throw new RuntimeException("Invalid URL read from database: " + r.get(0, String.class), e);
                    }
                });
    }

    /**
     * Retrieves the list of parent documents for the specified document ID.
     * @param docId The document ID to retrieve the parents for
     * @return A list of parent documents for the specified document ID; or an empty list if the document ID does not exist
     */
    public List<Document> parents(int docId) {
        if (!hasDocId(docId)) {
            return List.of();
        }
        return create.select(DSL.field(DSL.name("docId"))).from(DSL.table(DSL.name("DocumentLink")))
                .where(DSL.condition(DSL.field(DSL.name("childUrl"))
                        .eq(getDocFromId(docId).url().toString())))
                .fetch().map(r -> getDocFromId(r.get(0, Integer.class)));
    }

    /**
     * Retrieves the list of parent documents for the specified URL.
     * @param url The URL to retrieve the parents for
     * @return A list of parent documents for the specified document ID
     */
    public List<Document> parents(URL url) {
        return create.select(DSL.field(DSL.name("docId"))).from(DSL.table(DSL.name("DocumentLink")))
                .where(DSL.condition(DSL.field(DSL.name("childUrl"))
                        .eq(url.toString())))
                .fetch().map(r -> getDocFromId(r.get(0, Integer.class)));
    }

    /**
     * Drops all the word frequency records associated with this document ID.
     *
     * <p>This also has a significant performance impact - use with care.
     * @param docId The document ID to purge frequencies for
     */
    public void deleteFrequencies(int docId) {
        bodyOperator().deleteFrequencies(docId);
        titleOperator().deleteFrequencies(docId);
    }

    /**
     * Drops all the children associated with this document ID.
     * This means that all records in the document link table with the
     * specified DocId field should be dropped.
     * @param docId The document ID to purge links for
     */
    public void deleteChildren(int docId) {
        create.delete(DSL.table("DocumentLink"))
                .where(DSL.condition(DSL.field(DSL.name("docId")).eq(docId)))
                .execute();
    }

    /**
     * Gets the TableOperation object which operates on the tables
     * which represent words in the body of a document.
     * @return The TableOperation object to operate on tables associated
     *         with document bodies
     */
    public TableOperation bodyOperator() {
        return new BodyTableOperation(create);
    }

    /**
     * Gets the TableOperation object which operates on the tables
     * which represent words in the title of a document.
     * @return The TableOperation object to operate on tables associated
     *         with document titles
     */
    public TableOperation titleOperator() {
        return new TitleTableOperation(create);
    }

    /**
     * Gets the next document ID, and increments the next document ID number by one.
     *
     * <p>The next document ID is synchronized across all database connections
     * and is not refreshed with commits. It is only read once per application
     * startup. Specifically, it is only read once on the first constructor invocation
     * of this class.
     * @throws IllegalStateException If no instances of this class has been created yet
     */
    public static int nextDocId() {
        if (nextDocId == null) {
            throw new IllegalStateException("No database connection initialized." +
                    "Please create an instance of DatabaseConnection first.");
        }
        return nextDocId.getAndIncrement();
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
