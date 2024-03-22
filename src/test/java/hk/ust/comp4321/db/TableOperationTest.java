package hk.ust.comp4321.db;

import hk.ust.comp4321.api.WordInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
    void setUp() throws IOException, SQLException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
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
    void getTableNames() {
        assertEquals(4, body.getTableNames().size());
        assertEquals(3, title.getTableNames().size());

        conn.deleteFrequencies(1); // deleting should not be destructive
        assertEquals(4, body.getTableNames().size());
        assertEquals(3, title.getTableNames().size());
    }

    @Test
    void getStemIds() {
        assertEquals(4, body.getStemIds().size());
        assertEquals(3, title.getStemIds().size());

        conn.deleteFrequencies(0);
        assertEquals(4, body.getStemIds().size());
        assertEquals(3, title.getStemIds().size());
    }

    @Test
    void getNextId() {
        assertEquals(4, body.getNextId());
        assertEquals(3, title.getNextId());

        body.getNextId();
        body.getNextId();

        assertEquals(7, body.getNextId());
        assertEquals(4, title.getNextId());
    }

    @Test
    void getPrefix() {
        assertEquals("body", body.getPrefix());
        assertEquals("title", title.getPrefix());
    }

    @Test
    void insertWordInfo() {
        body.insertWordInfo(1, new WordInfo(0, 0, 0, 0, "location"));
        title.insertWordInfo(2, new WordInfo(0, 0, 0, 0, "opportunist"));

        assertTrue(body.getFrequency(1).contains(new WordInfo(0, 0, 0, 0, "location")));
        assertTrue(title.getFrequency(2).contains(new WordInfo(0, 0, 0, 0, "opportunist")));
        assertFalse(title.getFrequency(2).contains(new WordInfo(0, 0, 0, 0, "opportunion")));

        assertDoesNotThrow(() -> body.insertWordInfo(1, new WordInfo(0, 0, 0, 0, "location")));
        assertDoesNotThrow(() -> title.insertWordInfo(0, new WordInfo(0, 1, 1, 1, "computing")));
    }

    @Test
    void getFrequency() {
        assertTrue(body.getFrequency(0).contains(new WordInfo(1, 3270972, 2, 1, "computer")));
        assertFalse(body.getFrequency(0).contains(new WordInfo(1, 3270972, 2, 239040, "computer")));
    }

    @Test
    void deleteFrequencies() {
        body.deleteFrequencies(0);
        assertTrue(body.getFrequency(body.getIdFromStem("comput"))
                .stream().noneMatch(w -> w.docId() == 0)); // body tables don't have docId == 0
        assertTrue(body.getFrequency(body.getIdFromStem("comput")).stream().noneMatch(w -> w.docId() == 0)); // title table don't have docId == 0
        assertEquals(2, body.getFrequency(body.getIdFromStem("comput")).size()); // 2 frequency records remaining

        title.deleteFrequencies(0);
        assertEquals(1, title.getFrequency(title.getIdFromStem("comput")).size());
    }

    @Test
    void getIdFromStem() {
        // existing ones
        assertEquals(1, body.getIdFromStem("locat"));
        assertEquals(0, title.getIdFromStem("comput"));

        assertEquals(3, body.getIdFromStem("educ"));
        assertEquals(2, title.getIdFromStem("opportun"));

        // non-duplicates
        assertEquals(-1, body.getIdFromStem("societi"));
        assertEquals(-1, title.getIdFromStem("superl"));
    }

    @Test
    void getStemFromId() {
        assertEquals("locat", body.getStemFromId(1));
        assertEquals("locat", title.getStemFromId(1));

        assertEquals("engin", body.getStemFromId(2));
        assertEquals("opportun", title.getStemFromId(2));

        assertThrows(IllegalArgumentException.class, () -> body.getStemFromId(1000));
        assertThrows(IllegalArgumentException.class, () -> title.getStemFromId(3));
    }

    @Test
    void insertStem() {
        // duplicates
        assertEquals(0, body.insertStem("comput"));
        assertEquals(1, title.insertStem("locat"));

        assertEquals(3, body.insertStem("educ"));
        assertEquals(2, title.insertStem("opportun"));

        // non-duplicates
        assertEquals(4, body.insertStem("societi"));
        assertEquals(3, title.insertStem("superl"));
    }
}