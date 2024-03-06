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
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConnectionTest {
    private static void resetId() throws NoSuchFieldException, IllegalAccessException {
        ReflectUtil.setStaticField("nextId", null, DatabaseConnection.class);
    }

    DatabaseConnection conn;
    @BeforeEach
    void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
        resetId();
        conn = new DatabaseConnection(Path.of("test.db"));
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
    }

    @Test
    void getDocFromId() throws NoSuchFieldException, IllegalAccessException, SQLException, MalformedURLException {
        resetId();
        conn = new DatabaseConnection(Path.of("test-data/test-data.db"));
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
    }

    @Test
    void commit() {
    }

    @Test
    void children() {

    }

    @Test
    void nextDocId() throws NoSuchFieldException, IllegalAccessException, SQLException {
        assertEquals(0, DatabaseConnection.nextDocId());
        assertEquals(1, DatabaseConnection.nextDocId());

        resetId();
        conn = new DatabaseConnection(Path.of("test-data/test-data.db"));
        assertEquals(2, DatabaseConnection.nextDocId());
        assertEquals(3, DatabaseConnection.nextDocId());
    }
}