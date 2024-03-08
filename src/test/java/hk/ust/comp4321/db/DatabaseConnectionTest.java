package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.test.ReflectUtil;
import org.jooq.DSLContext;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {
    private static void resetId() throws NoSuchFieldException, IllegalAccessException {
        ReflectUtil.setStaticField("nextId", null, DatabaseConnection.class);
    }

    private void connectEmpty() throws SQLException {
        conn = new DatabaseConnection(Path.of("empty.db"));
    }

    DatabaseConnection conn;
    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        resetId();
        conn = new DatabaseConnection(Path.of("test.db"));
        Connection connect = conn.getConnection();
        Statement state = connect.createStatement();
        state.execute("CREATE TABLE Comput_body (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");
        state.execute("CREATE TABLE Comput_title (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");
        state.execute("CREATE TABLE Locat_title (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer);");
        state.execute("CREATE TABLE Locat_body (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");

        PreparedStatement insert = connect.prepareStatement("INSERT INTO Comput_body VALUES (?, ?, ?, ?)");

        List<List<Integer>> computEntries = List.of(List.of(0, 1, 1, 1), List.of(0, 1, 2, 3), List.of(0, 99, 2, 3),
                List.of(1, 3, 2, 1), List.of(1, 3270972, 2, 1));
        insertInto(insert, computEntries);

        insert = connect.prepareStatement("INSERT INTO Comput_title VALUES (?, ?, ?, ?)");

        List<List<Integer>> computTitles = List.of(List.of(0, 1, 1, 1), List.of(1, 1, 1, 1));
        insertInto(insert, computTitles);

        List<DocumentTuple> docs = List.of(
                new DocumentTuple("https://www.cse.ust.hk/~kwtleung/", 0, Instant.ofEpochMilli(1709693690504L), 25565),
                new DocumentTuple("https://www.w3schools.com/sql/sql_insert.asp", 1, Instant.ofEpochMilli(1709693690504L), 25565),
                new DocumentTuple("https://sqlite.org/lang_datefunc.html", 2, Instant.ofEpochMilli(93690504), 2639425),
                new DocumentTuple("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/package-summary.html", 3, Instant.ofEpochMilli(95023232344L), 263942533),
                new DocumentTuple("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#currentTimeMillis()", 4, Instant.ofEpochMilli(93690504), 2639425)
        );

        insertDoc(DSL.using(conn.getConnection()), docs);

        insert = connect.prepareStatement("INSERT INTO DocumentLink VALUES (?, ?)");

        List<List<Integer>> links = List.of(List.of(0, 1), List.of(0, 2), List.of(0, 3), List.of(0, 4),
                List.of(4, 0), List.of(4, 2), List.of(3, 1), List.of(3, 3));
        insertInto(insert, links);

        conn.close();

        /*
         * Note: Since we bypassed all proper APIs to insert Documents into the database,
         * the Doc IDs are wrong. We close and reopen the connection to fix this problem.
         */
        resetId();
        conn = new DatabaseConnection(Path.of("test.db"));
    }

    private static void insertInto(PreparedStatement insert, List<List<Integer>> data) {
        data.forEach(l -> {
            IntStream.range(0, l.size()).forEach(i -> {
                try {
                    insert.setInt(i + 1, l.get(i));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                insert.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void insertDoc(DSLContext create, List<DocumentTuple> tuple) {
        tuple.forEach(doc -> create.insertInto(DSL.table("Document"))
                        .values(doc.url, doc.docId, doc.lastMod, doc.size)
                                .execute());
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
        Files.deleteIfExists(Path.of("empty.db"));
    }

    @Test
    void getDocFromId() throws MalformedURLException {
        assertThrows(IllegalArgumentException.class, () -> conn.getDocFromId(1000));
        Document doc = conn.getDocFromId(0);
        assertEquals(0, doc.id());
        assertEquals(new URL("https://www.cse.ust.hk/~kwtleung/"), doc.url());
    }

    @Test
    void insertLink() {
        assertThrows(IntegrityConstraintViolationException.class, () -> conn.insertLink(1000, 1000)); // nonexistent docId should not work
        conn.insertLink(2, 3);
        assertTrue(conn.children(2).stream().anyMatch(doc -> doc.id() == 3)); // normal insert link
        assertDoesNotThrow(() -> conn.insertLink(2, 3)); // multiple insertions do not crash
        assertEquals(1, conn.children(2).size()); // multiple insertions do not crash
    }

    @Test
    void deleteFrequencies() {
        assertDoesNotThrow(() -> conn.deleteFrequencies(1000)); // deleting nonexistent ID does not fail
        conn.deleteFrequencies(0);
        assertTrue(conn.bodyOperator().getFrequency("Comput").stream().noneMatch(w -> w.docId() == 0)); // body tables don't have docId == 0
        assertTrue(conn.titleOperator().getFrequency("Comput").stream().noneMatch(w -> w.docId() == 0)); // title table don't have docId == 0
        assertEquals(2, conn.bodyOperator().getFrequency("Comput").size()); // 2 frequency records remaining
    }

    @Test
    void deleteChildren() {
        assertDoesNotThrow(() -> conn.deleteChildren(1000)); // Invalid ID
        conn.deleteChildren(0);
        assertEquals(0, conn.children(0).size()); // Dropped tables should have no children left
        assertEquals(2, conn.children(3).size()); // Unaffected tables should be unaffected
        assertDoesNotThrow(() -> conn.deleteChildren(0)); // Deleting the same thing shouldn't crash
    }

    @Test
    void children() {
        assertEquals(4, conn.children(0).size()); // 4 children, as expected
        assertEquals(0, conn.children(1000).size()); // Non-existent IDs should return 0
    }

    @Test
    void parents() {
        assertEquals(2, conn.parents(2).size()); // 2 parents, as expected
        assertEquals(0, conn.parents(1000).size()); // Non-existent IDs should return 0
    }

    @Test
    void nextDocId() throws NoSuchFieldException, IllegalAccessException, SQLException {
        assertEquals(5, DatabaseConnection.nextDocId()); // currently 5 docs, so next ID is 5
        assertEquals(6, DatabaseConnection.nextDocId()); // 6 after last allocation

        resetId();
        connectEmpty();
        assertEquals(0, DatabaseConnection.nextDocId()); // 0 since we connected to an empty DB
        assertEquals(1, DatabaseConnection.nextDocId()); // 1 after last allocation
    }

    @Test
    void insertDocument() throws MalformedURLException {
        Instant prev = Instant.now();
        conn.insertDocument(new Document(URI.create("https://github.com/151044/COMP4321-G42/pull/2").toURL(), DatabaseConnection.nextDocId(), prev, 234324444L));
        assertDoesNotThrow(() -> conn.getDocFromId(5)); // successful insertion
        conn.insertDocument(new Document(URI.create("https://github.com/151044/COMP4321-G42/pull/2").toURL(), 5, Instant.now(), 2));
        assertTrue(prev.isBefore(conn.getDocFromId(5).lastModified())); // checks if we have correctly updated on duplicate insert
        assertEquals(2, conn.getDocFromId(5).size()); // ditto
    }

    /**
     * The internal representation of a Document object for testing purposes only.
     *
     * <p>This class exists solely for testing. Please do not use it unless you are
     * batching test updates to the document table.
     * @param url The URL of the document
     * @param docId The document ID of the document
     * @param lastMod The last modified date of the document
     * @param size The size of the document
     */
    private record DocumentTuple(String url, int docId, Instant lastMod, long size) {}
}