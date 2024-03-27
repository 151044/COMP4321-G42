package hk.ust.comp4321.spider;

import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.DbUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SpiderTest {

    URL testLink;
    Spider testSpider;
    DatabaseConnection conn;

    @BeforeEach
    void setUp() throws IOException, SQLException, URISyntaxException, NoSuchFieldException, IllegalAccessException {
        testLink = new URL("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm");
        conn = DbUtil.initializeTestDb();
        testSpider = new Spider(testLink, conn);
    }

    @AfterEach
    void tearDown() throws SQLException, IOException {
        conn.close();
        Files.deleteIfExists(Path.of("test.db"));
    }

    @Test
    void discover() {
        //System.out.println(testSpider.discover(Spider.StopType.INDEXED, 30));
        assertEquals(30, testSpider.discover(Spider.StopType.INDEXED, 30).size());
    }
}