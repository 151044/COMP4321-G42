package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.DbUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void retrieveFromDataBaseTitle() throws IOException, SQLException {
        int docID = DatabaseConnection.nextDocId();

        Document dummyDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        dummyDoc.retrieveFromWeb();
        dummyDoc.writeWords(conn);

        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        testDoc.retrieveFromDatabase(conn);

        Map<String, WordInfo> expectedMap = new HashMap<>();
        expectedMap.put("test", new WordInfo(5, 0, 0, 0, ""));
        expectedMap.put("page", new WordInfo(5, 0, 0,1, ""));

        assertEquals(expectedMap, testDoc.titleFrequencies());
    }

    @Test
    void retrieveFromDataBaseBody() throws IOException, SQLException {
        int docID = DatabaseConnection.nextDocId();

        Document dummyDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        dummyDoc.retrieveFromWeb();
        dummyDoc.writeWords(conn);

        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        testDoc.retrieveFromDatabase(conn);

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

    @Test
    void retrieveFromDataBaseURL() throws IOException, SQLException {
        Document ustCse = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 392L);
        Document news = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/news.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 384L);
        Document books = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/books.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 601L);
        Document movies = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 19080);

//        ustCse.retrieveFromWeb();
        ustCse.writeWords(conn);
//        news.retrieveFromWeb();
        news.writeWords(conn);
//        books.retrieveFromWeb();
        books.writeWords(conn);
//        movies.retrieveFromWeb();
        movies.writeWords(conn);

        int docID = DatabaseConnection.nextDocId();

        Document dummyDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        dummyDoc.retrieveFromWeb();
        dummyDoc.writeWords(conn);
        dummyDoc.writeChildrenLinks(conn);

        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), docID, Instant.now(), 603L);
        testDoc.retrieveFromDatabase(conn);

        assertTrue(dummyDoc.children().containsAll(testDoc.children()) && testDoc.children().containsAll(dummyDoc.children()));
    }

    @Test
    void retrieveFromWebTitle() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
        testDoc.retrieveFromWeb();
        Map<String, WordInfo> expectedMap = new HashMap<>();
        expectedMap.put("test", new WordInfo(5, 0, 0, 0, ""));
        expectedMap.put("page", new WordInfo(5, 0, 0,1, ""));
        assertEquals(expectedMap, testDoc.titleFrequencies());
    }

    @Test
    void retrieveFromWebBody() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
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

    @Test
    void retrieveFromWebURL() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
        testDoc.retrieveFromWeb();
        List<URL> expectedChildren = List.of(
                URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm").toURL(),
                URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/news.htm").toURL(),
                URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/books.htm").toURL(),
                URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm").toURL()
        );
        assertEquals(expectedChildren, testDoc.children());
    }
}
