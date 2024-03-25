package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.DbUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentTest {

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
    void retrieveFromWebTitle() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
        testDoc.writeWords(conn);
        testDoc.retrieveFromWeb();
        Map<String, WordInfo> expectedMap = new HashMap<>();
        expectedMap.put("test", new WordInfo(5, 0, 0, 0, ""));
        expectedMap.put("page", new WordInfo(5, 0, 0,1, ""));
        assertEquals(expectedMap, testDoc.titleFrequencies());
    }

    @Test
    void retrieveFromWebBody() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
        testDoc.writeWords(conn);
        testDoc.retrieveFromWeb();

        Map<String, WordInfo> expectedMap = new HashMap<>();

        expectedMap.put("test", new WordInfo(5, 0, 0, 3, ""));
        expectedMap.put("page", new WordInfo(5, 0, 0, 4, ""));
        expectedMap.put("crawler", new WordInfo(5, 0, 0,7, ""));
        expectedMap.put("gett", new WordInfo(5, 1, 0,1, "getting"));
        expectedMap.put("admission", new WordInfo(5, 1, 0, 3, ""));
        expectedMap.put("cse", new WordInfo(5, 1, 0,5, ""));
        expectedMap.put("depart", new WordInfo(5, 1, 0, 6, "department"));
        expectedMap.put("hkust", new WordInfo(5, 1, 0,8, ""));
        expectedMap.put("read", new WordInfo(5, 1, 0, 11, ""));
        expectedMap.put("internat", new WordInfo(5, 1, 0,14, "international"));
        expectedMap.put("news", new WordInfo(5, 1, 0,15, ""));
        expectedMap.put("book", new WordInfo(5, 1, 0,18, "books"));
        expectedMap.put("movi", new WordInfo(5, 2, 0,3, "movie"));
        expectedMap.put("list", new WordInfo(5, 2, 0,4, ""));
        expectedMap.put("new", new WordInfo(5, 2, 0,5, ""));

        assertEquals(expectedMap, testDoc.bodyFrequencies());
    }
}
