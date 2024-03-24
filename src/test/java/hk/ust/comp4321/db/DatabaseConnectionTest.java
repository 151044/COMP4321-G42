package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.test.ReflectUtil;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {
    private static void resetId() throws NoSuchFieldException, IllegalAccessException {
        ReflectUtil.setStaticField("nextDocId", null, DatabaseConnection.class);
    }

    private void connectEmpty() throws SQLException {
        conn = new DatabaseConnection(Path.of("empty.db"));
    }

    DatabaseConnection conn;
    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
        conn = DbUtil.initializeTestDb();
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
        Files.deleteIfExists(Path.of("empty.db"));
    }

    @Test
    void getDocFromId() throws MalformedURLException, URISyntaxException {
        assertThrows(IllegalArgumentException.class, () -> conn.getDocFromId(1000));
        Document doc = conn.getDocFromId(0);
        assertEquals(0, doc.id());
        assertEquals(new URI("https://www.cse.ust.hk/~kwtleung/").toURL(), doc.url());
    }

    @Test
    void insertLink() throws URISyntaxException, MalformedURLException {
        URL url = new URI("https://www.google.com/").toURL();
        assertThrows(IntegrityConstraintViolationException.class, () -> conn.insertLink(1000, url)); // nonexistent docId should not work
        conn.insertLink(2, url);
        assertTrue(conn.children(2).stream().anyMatch(urls -> urls.toString().equals(url.toString()))); // normal insert link
        assertDoesNotThrow(() -> conn.insertLink(2, url)); // multiple insertions do not crash
        assertEquals(1, conn.children(2).size()); // multiple insertions do not crash
    }

    @Test
    void deleteFrequencies() {
        assertDoesNotThrow(() -> conn.deleteFrequencies(1000)); // deleting nonexistent ID does not fail
        conn.deleteFrequencies(0);
        TableOperation bodyOperator = conn.bodyOperator();
        assertTrue(bodyOperator.getFrequency(bodyOperator.getIdFromStem("comput"))
                .stream().noneMatch(w -> w.docId() == 0)); // body tables don't have docId == 0
        assertTrue(bodyOperator.getFrequency(bodyOperator.getIdFromStem("comput")).stream().noneMatch(w -> w.docId() == 0)); // title table don't have docId == 0
        assertEquals(2, bodyOperator.getFrequency(bodyOperator.getIdFromStem("comput")).size()); // 2 frequency records remaining
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
    void parents() throws URISyntaxException, MalformedURLException {
        assertEquals(2, conn.parents(2).size()); // 2 parents, as expected
        assertEquals(0, conn.parents(1000).size()); // Non-existent IDs should return 0

        assertEquals(2, conn.parents(new URI("https://sqlite.org/lang_datefunc.html").toURL()).size());
        assertEquals(0, conn.parents(new URI("https://github.com/151044/COMP4321-G42/").toURL()).size());
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

    @Test
    void getDocFromUrl() throws URISyntaxException, MalformedURLException {
        assertThrows(IllegalArgumentException.class, () ->
                conn.getDocFromUrl(new URI("https://github.com/151044/COMP4321-G42/").toURL())); // invalid URL throws an exception
        assertEquals(2, conn.getDocFromUrl(new URI("https://sqlite.org/lang_datefunc.html").toURL()).id()); // normal use case
    }

    @Test
    void hasDocId() {
        assertTrue(conn.hasDocId(2));
        assertFalse(conn.hasDocId(69));
    }
}