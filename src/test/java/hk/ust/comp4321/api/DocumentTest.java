package hk.ust.comp4321.api;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.DbUtil;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

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

        Map<WordInfo, String> expectedMap = new HashMap<>();
        expectedMap.put(new WordInfo(5, 0, 0, 0, ""), "test");
        expectedMap.put(new WordInfo(5, 0, 0,1, ""), "page");

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

        Map<WordInfo, String> expectedMap = new HashMap<>();
        expectedMap.put(new WordInfo(5, 0, 0, 3, ""), "test");
        expectedMap.put(new WordInfo(5, 0, 0, 4, ""), "page");
        expectedMap.put(new WordInfo(5, 0, 0,7, ""), "crawler");
        expectedMap.put(new WordInfo(5, 1, 0,1, "getting"), "gett");
        expectedMap.put(new WordInfo(5, 1, 0, 3, ""), "admission");
        expectedMap.put(new WordInfo(5, 1, 0,5, ""), "cse");
        expectedMap.put(new WordInfo(5, 1, 0, 6, "department"), "depart");
        expectedMap.put(new WordInfo(5, 1, 0,8, ""), "hkust");
        expectedMap.put(new WordInfo(5, 1, 0, 11, ""), "read");
        expectedMap.put(new WordInfo(5, 1, 0,14, "international"), "internat");
        expectedMap.put(new WordInfo(5, 1, 0,15, ""), "news");
        expectedMap.put(new WordInfo(5, 1, 0,18, "books"), "book");
        expectedMap.put(new WordInfo(5, 2, 0,3, "movie"), "movi");
        expectedMap.put(new WordInfo(5, 2, 0,4, ""), "list");
        expectedMap.put(new WordInfo(5, 2, 0,5, ""), "new");

        assertEquals(expectedMap, testDoc.bodyFrequencies());
    }

    @Test
    void retrieveFromDataBaseURL() throws IOException, SQLException {
        Document ustCse = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 392L);
        Document news = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/news.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 384L);
        Document books = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/books.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 601L);
        Document movies = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/Movie.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 19080);

        ustCse.retrieveFromWeb();
        ustCse.writeWords(conn);
        news.retrieveFromWeb();
        news.writeWords(conn);
        books.retrieveFromWeb();
        books.writeWords(conn);
        movies.retrieveFromWeb();
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
        Map<WordInfo, String> expectedMap = new HashMap<>();
        expectedMap.put(new WordInfo(5, 0, 0, 0, ""), "test");
        expectedMap.put(new WordInfo(5, 0, 0,1, ""), "page");
        assertEquals(expectedMap, testDoc.titleFrequencies());
    }

    @Test
    void retrieveFromWebBody() throws IOException {
        Document testDoc = new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 603L);
        testDoc.retrieveFromWeb();

        Map<WordInfo, String> expectedMap = new HashMap<>();
        expectedMap.put(new WordInfo(5, 0, 0, 3, ""), "test");
        expectedMap.put(new WordInfo(5, 0, 0, 4, ""), "page");
        expectedMap.put(new WordInfo(5, 0, 0,7, ""), "crawler");
        expectedMap.put(new WordInfo(5, 1, 0,1, "getting"), "gett");
        expectedMap.put(new WordInfo(5, 1, 0, 3, ""), "admission");
        expectedMap.put(new WordInfo(5, 1, 0,5, ""), "cse");
        expectedMap.put(new WordInfo(5, 1, 0, 6, "department"), "depart");
        expectedMap.put(new WordInfo(5, 1, 0,8, ""), "hkust");
        expectedMap.put(new WordInfo(5, 1, 0, 11, ""), "read");
        expectedMap.put(new WordInfo(5, 1, 0,14, "international"), "internat");
        expectedMap.put(new WordInfo(5, 1, 0,15, ""), "news");
        expectedMap.put(new WordInfo(5, 1, 0,18, "books"), "book");
        expectedMap.put(new WordInfo(5, 2, 0,3, "movie"), "movi");
        expectedMap.put(new WordInfo(5, 2, 0,4, ""), "list");
        expectedMap.put(new WordInfo(5, 2, 0,5, ""), "new");

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

    @Test
    void retrieveFromWebGeneral() throws IOException {
        // This page contains a child link that causes MalformedURLException, which should be handled by the document method
        Document testDoc1 = new Document(URI.create("https://hkust.edu.hk/").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 18180L);
        testDoc1.retrieveFromWeb();
        assertTrue(testDoc1.isLoaded());

        // This page does not contain a body section, which should be handled by the document method and skip the body retrieval
        Document testDoc2 = new Document(URI.create("https://www.math.hkust.edu.hk/~mamu/").toURL(), DatabaseConnection.nextDocId(), Instant.now(), 37540L);
        testDoc2.retrieveFromWeb();
        assertTrue(testDoc2.isLoaded());

        // This page is a PDF file that is not supported by Jsoup, which should be ignored by the document method and hence not loaded
        Document testDoc3 = new Document(URI.create("https://www.math.hkust.edu.hk/~makyli/art6.pdf").toURL(), DatabaseConnection.nextDocId(), Instant.now(), -69420L);
        testDoc3.retrieveFromWeb();
        assertFalse(testDoc3.isLoaded());
    }
}
