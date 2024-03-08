package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.net.MalformedURLException;
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
 * <p>Currently, the database is organized into three different types of tables -
 * the Document table, the Document Link table and Word (title) tables.
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
 * is a very dense read.
 */
public class DatabaseConnection implements AutoCloseable {
    private final Connection conn;
    private static AtomicInteger nextId = null;
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
        create.createTableIfNotExists("Document")
                .column("url", VARCHAR)
                .column("docId", INTEGER)
                .column("lastModified", INSTANT)
                .column("size", BIGINT)
                .constraint(
                        DSL.primaryKey("docId")
                ).execute();
        create.createTableIfNotExists("DocumentLink")
                .column("docId", INTEGER)
                .column("childId", INTEGER)
                .constraints(
                        DSL.primaryKey("docId", "childId"),
                        DSL.foreignKey("docId").references("Document", "docId")
                )
                .execute();

        if (nextId == null) {
            nextId = new AtomicInteger(create.fetchCount(DSL.table("Document")));
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
                        DSL.condition("docId = " + docId)
                ).fetch().stream().findFirst()
                .map(r -> {
                    try {
                        return new Document(new URL(r.get(0, String.class)),
                                r.get(1, Integer.class),
                                r.get(2, Instant.class),
                                r.get(3, Long.class));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("No such document ID: " + docId));
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
                .values(doc.url().toString(), doc.id(), doc.lastModified(), doc.size())
                .onDuplicateKeyUpdate()
                .set(DSL.field("lastModified", INSTANT), doc.lastModified())
                .set(DSL.field("size", BIGINT), doc.size())
                .execute();
    }

    /**
     * Inserts a link into the document link database.
     * @param docId The parent document ID
     * @param child The child document ID
     */
    public void insertLink(int docId, int child) {
        create.insertInto(DSL.table("DocumentLink"))
                .values(docId, child)
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Retrieves the list of children documents for the specified document ID.
     * @param docId The document ID to retrieve the children for
     * @return A list of child documents for the specified document ID
     */
    public List<Document> children(int docId) {
        return create.select(DSL.field("childId")).from(DSL.table("DocumentLink"))
                .where(DSL.condition("docId = " + docId))
                .fetch().map(r -> getDocFromId(r.get(0, Integer.class)));
    }

    /**
     * Retrieves the list of parent documents for the specified document ID.
     * @param docId The document ID to retrieve the parents for
     * @return A list of parent documents for the specified document ID
     */
    public List<Document> parents(int docId) {
        return create.select(DSL.field("docId")).from(DSL.table("DocumentLink"))
                .where(DSL.condition("childId = " + docId))
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
                .where(DSL.condition("docId = " + docId))
                .execute();
    }

    /**
     * Gets the TableOperation object which operates on the tables
     * which represent words in the body of a document.
     * @return The TableOperation object to operate on tables associated
     *         with document bodies
     */
    public TableOperation bodyOperator() {
        return new BodyTableOperation(conn);
    }

    /**
     * Gets the TableOperation object which operates on the tables
     * which represent words in the title of a document.
     * @return The TableOperation object to operate on tables associated
     *         with document titles
     */
    public TableOperation titleOperator() {
        return new TitleTableOperation(conn);
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
     * Gets the next document ID, and increments the next document ID number by one.
     *
     * <p>The next document ID is synchronized across all database connections
     * and is not refreshed with commits. It is only read once per application
     * startup. Specifically, it is only read once on the first constructor invocation
     * of this class.
     * @throws IllegalStateException If no instances of this class has been created yet
     */
    public static int nextDocId() {
        if (nextId == null) {
            throw new IllegalStateException("No database connection initialized." +
                    "Please create an instance of DatabaseConnection first.");
        }
        return nextId.getAndIncrement();
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
