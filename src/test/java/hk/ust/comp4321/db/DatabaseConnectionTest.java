package hk.ust.comp4321.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConnectionTest {

    DatabaseConnection conn;
    @BeforeEach
    void setUp() throws SQLException {
        conn = new DatabaseConnection(Path.of("test.db"));
    }

    @AfterEach
    void tearDown() {
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
    }
}