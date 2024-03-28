package hk.ust.comp4321.spider;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SpiderTest {

    Path testPath = Path.of("spiderTest.db");
    URL testLink, testLinkExtra;
    Spider testSpider, testSpiderExtra;
    DatabaseConnection conn;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        Files.deleteIfExists(testPath);
        testLink = new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
        testLinkExtra = new URL("https://www.google.com/comp4321");
        conn = new DatabaseConnection(testPath);
        testSpider = new Spider(testLink, conn);
        testSpiderExtra = new Spider(testLinkExtra, conn);
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(testPath);
    }

    @Test
    void discover() throws IOException {
        // Crawl 10 pages
        assertEquals(10, testSpider.discover(10).size());

        // Modify the last modified date of a child page
        conn.insertDocument(new Document(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm").toURL(),1, Instant.ofEpochMilli(4321L), 603L));
        // Spider should update the child page, and crawl 2 new pages
        assertEquals(List.of(new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/ust_cse.htm"),
                new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book2.htm"),
                new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/books/book3.htm")), testSpider.discover(3));

        // HTTP status code check and total pages < threshold break test
        assertEquals(List.of(), testSpiderExtra.discover(2));
    }
}