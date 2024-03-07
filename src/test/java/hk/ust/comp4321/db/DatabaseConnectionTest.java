package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.test.ReflectUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
        state.execute("CREATE TABLE Comput (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");
        state.execute("CREATE TABLE Comput_title (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");
        state.execute("CREATE TABLE Locat_title (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer);");
        state.execute("CREATE TABLE Locat (DocId Integer, Paragraph Integer, Sentence Integer, Location Integer)");

        PreparedStatement insert = connect.prepareStatement("INSERT INTO Comput VALUES (?, ?, ?, ?)");

        List<List<Integer>> computEntries = List.of(List.of(0, 1, 1, 1), List.of(0, 1, 2, 3), List.of(0, 99, 2, 3),
                List.of(1, 3, 2, 1), List.of(1, 3270972, 2, 1));
        insertInto(insert, computEntries);

        insert = connect.prepareStatement("INSERT INTO Comput_title VALUES (?, ?, ?, ?)");

        List<List<Integer>> computTitles = List.of(List.of(0, 1, 1, 1), List.of(1, 1, 1, 1));
        insertInto(insert, computTitles);

        List<DocumentTuple> docs = List.of(
                new DocumentTuple("https://www.cse.ust.hk/~kwtleung/", 0, 1709693690504L, 25565),
                new DocumentTuple("https://www.w3schools.com/sql/sql_insert.asp", 1, 1709693690504L, 25565),
                new DocumentTuple("https://sqlite.org/lang_datefunc.html", 2, 93690504, 2639425),
                new DocumentTuple("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/package-summary.html", 3, 95023232344L, 263942533),
                new DocumentTuple("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#currentTimeMillis()", 4, 93690504, 2639425)
        );
        insert = connect.prepareStatement("INSERT INTO Document VALUES (?, ?, ?, ?)");

        insertDoc(insert, docs);

        insert = connect.prepareStatement("INSERT INTO DocumentLink VALUES (?, ?)");

        List<List<Integer>> links = List.of(List.of(0, 1), List.of(0, 2), List.of(0, 3), List.of(0, 4),
                List.of(4, 0), List.of(4, 2), List.of(3, 1), List.of(3, 3));
        insertInto(insert, links);

        state.close();
        conn.commit();
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

    private static void insertDoc(PreparedStatement stmt, List<DocumentTuple> tuple) {
        tuple.forEach(doc -> {
            try {
                stmt.setString(1, doc.url);
                stmt.setInt(2, doc.docId);
                stmt.setLong(3, doc.lastMod);
                stmt.setLong(4, doc.size);
                stmt.execute();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
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
    void insertWord() {

    }

    @Test
    void insertTitleWord() {
    }

    @Test
    void insertDocument() {

    }

    @Test
    void frequenciesOf() {
    }

    @Test
    void frequenciesOfTitle() {
    }

    @Test
    void insertLink() {
    }

    @Test
    void deleteFrequencies() {
    }

    @Test
    void deleteForwardLinks() {
        assertDoesNotThrow(() -> conn.deleteForwardLinks(1000)); // Invalid ID
        conn.deleteForwardLinks(0);
        assertEquals(0, conn.children(0).size()); // Dropped tables should have no children left
        assertEquals(2, conn.children(3).size()); // Unaffected tables should be unaffected
        assertDoesNotThrow(() -> conn.deleteForwardLinks(0)); // Deleting the same thing shouldn't crash
    }

    @Test
    void commit() {

    }

    @Test
    void children() {
        assertEquals(4, conn.children(0).size()); // 4 children, as expected
        assertEquals(0, conn.children(0).size()); // IDs which don't exist should return 0
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
    private record DocumentTuple(String url, int docId, long lastMod, long size) {}
}