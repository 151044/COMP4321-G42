package hk.ust.comp4321.db;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseConnectionTest {

    DatabaseConnection conn;
    @BeforeEach
    void setUp() throws SQLException {
        conn = new DatabaseConnection(Path.of("test.db"));
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
    }

    @Test
    void getDocFromId() {
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
    void nextDocId() {
        assertEquals(0, DatabaseConnection.nextDocId());

    }
}