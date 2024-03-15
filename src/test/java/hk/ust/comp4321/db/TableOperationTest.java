package hk.ust.comp4321.db;

import hk.ust.comp4321.test.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class TableOperationTest {
    private DatabaseConnection conn;
    private TableOperation body;
    private TableOperation title;
    @BeforeEach
    void setUp() throws MalformedURLException, SQLException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        conn = DbUtil.initializeTestDb();
        body = conn.bodyOperator();
        title = conn.titleOperator();
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
        Files.deleteIfExists(Path.of("empty.db"));
    }

    @Test
    void getStems() {
    }

    @Test
    void getNextId() {
    }

    @Test
    void getPrefix() {
        assertEquals("body", body.getPrefix());
        assertEquals("title", title.getPrefix());
    }

    @Test
    void insertWord() {
    }

    @Test
    void getFrequency() {
    }

    @Test
    void deleteFrequencies() {
    }

    @Test
    void getStemId() {
    }

    @Test
    void getStemFromId() {
    }

    @Test
    void insertStem() {
    }
}