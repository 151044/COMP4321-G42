package hk.ust.comp4321.spider;

import hk.ust.comp4321.db.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SpiderTest {

    Path testPath = Path.of("spiderTest.db");
    URL testLink;
    Spider testSpider;
    DatabaseConnection conn;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        Files.deleteIfExists(testPath);
        testLink = new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
        conn = new DatabaseConnection(testPath);
        testSpider = new Spider(testLink, conn);
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(testPath);
    }

    @Test
    void discover() throws IOException {
        assertEquals(30, testSpider.discover(30).size());
    }
}