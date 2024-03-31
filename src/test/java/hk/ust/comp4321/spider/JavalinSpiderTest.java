package hk.ust.comp4321.spider;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.test.ReflectUtil;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavalinSpiderTest {
    Path testPath = Path.of("spiderTest.db");
    URL testLink, testLinkExtra;
    Spider testSpider, testSpiderExtra;
    DatabaseConnection conn;
    Javalin app;

    @BeforeEach
    void setUp() throws IOException, SQLException, NoSuchFieldException, IllegalAccessException {
        // Reset next doc id
        ReflectUtil.setStaticField("nextDocId", null, DatabaseConnection.class);
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
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void redirectSimple() throws IOException, SQLException {
        app = Javalin.create()
                .get("/", ctx -> ctx.redirect("/home"))
                .get("/home", ctx -> ctx.redirect("/home/ust_student"))
                .get("/home/ust_student", ctx -> ctx.redirect("/home/cse"))
                .get("/home/cse", ctx -> {
                    ctx.html("""
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <title>Home Webpage of CSE; Home Webpage of HKUST CSE.</title>
                            </head>
                            
                            <body>
                            This is the home page of HKUST CSE.
                            <a href=../../>Go Home!</a>
                            </body>
                            """);
                }).start();
        Spider spider = new Spider(URI.create("http://localhost:" + app.port() + "/").toURL(), conn);
        spider.discover(3000);
        Document doc = conn.getDocFromId(0);
        doc.retrieveFromDatabase(conn);
        assertTrue(doc.titleFrequencies().containsValue("cse"));
    }

    @Test
    void redirectMulti() throws IOException, SQLException {
        int jumps = 100;
        app = Javalin.create()
                .get("/", ctx -> ctx.redirect("/home"))
                .get("/home", ctx -> ctx.redirect("/home/ust_student"))
                .get("/home/ust_student", ctx -> ctx.redirect("/home/cse"))
                .get("/home/cse", ctx -> {
                    ctx.html("""
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <title>Home Webpage of CSE; Home Webpage of HKUST CSE.</title>
                            </head>
                            
                            <body>
                            This is the home page of HKUST CSE.
                            <a href=/home/rabbit_hole>Go Home!</a>
                            </body>
                            """);
                });
        app = app.get("/home/rabbit_hole", ctx -> ctx.redirect("/home/rabbit_hole_0"));
        for (int i = 0; i < jumps; i++) {
            int finalI = i;
            app = app.get("/home/rabbit_hole_" + i, ctx -> ctx.redirect("/home/rabbit_hole_" + (finalI + 1)));
        }
        app.get("/home/rabbit_hole_" + jumps, ctx -> ctx.redirect("/home/cse")).start();
        Spider spider = new Spider(URI.create("http://localhost:" + app.port() + "/").toURL(), conn);
        spider.discover(3000);
        Document doc = conn.getDocFromId(0);
        doc.retrieveFromDatabase(conn);
        assertTrue(doc.titleFrequencies().containsValue("cse"));
    }


    @Test
    void timeout() throws IOException, SQLException {
        app = Javalin.create()
                .get("/", ctx -> ctx.redirect("/home/cse"))
                .get("/home/cse", ctx -> ctx.html("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Home Webpage of CSE; Home Webpage of HKUST CSE.</title>
                        </head>
                        
                        <body>
                        This is the home page of HKUST CSE.
                        <a href=../../>Go Home!</a>
                        <a href=/home/cse/timeout>Limbo</a>
                        </body>
                        """))
                .get("/home/cse/timeout", ctx -> {
                    Thread.sleep(60000);
                    ctx.html("Took you long enough!");
                }).start();
        Spider spider = new Spider(URI.create("http://localhost:" + app.port() + "/home/cse").toURL(), conn);
        spider.discover(3000);
        Document doc = conn.getDocFromId(0);
        doc.retrieveFromDatabase(conn);
        assertTrue(doc.titleFrequencies().containsValue("cse"));
        assertEquals(2, conn.getDocuments().size());
    }

    @Test
    void failedStatuses() throws SQLException, IOException {
        app = Javalin.create()
                .get("/", ctx -> ctx.redirect("/home/cse"))
                .get("/home/cse", ctx -> ctx.html("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Home Webpage of CSE; Home Webpage of HKUST CSE.</title>
                        </head>
                        
                        <body>
                        This is the home page of HKUST CSE.
                        <a href=../../>Go Home!</a>
                        <a href=/home/cse/coffee>Coffee?</a>
                        <a href=/home/cse/rage>Angry?</a>
                        <a href=/home/cse/nobody>I want nobody nobody but you!</a>
                        </body>
                        """))
                .get("/home/cse/coffee", ctx -> ctx.status(HttpStatus.IM_A_TEAPOT))
                .get("/home/cse/rage", ctx -> ctx.status(HttpStatus.ENHANCE_YOUR_CALM))
                .get("/home/cse/nobody", ctx -> ctx.status(HttpStatus.OK))
                .start();
        Spider spider = new Spider(URI.create("http://localhost:" + app.port() + "/home/cse").toURL(), conn);
        spider.discover(3000);
        Document doc = conn.getDocFromId(0);
        doc.retrieveFromDatabase(conn);
        assertTrue(doc.titleFrequencies().containsValue("cse"));
        assertEquals(3, conn.getDocuments().size());
    }
    @Test
    void mimeTypes() throws SQLException, IOException {
        app = Javalin.create()
                .get("/", ctx -> ctx.redirect("/home/cse"))
                .get("/home/cse", ctx -> ctx.html("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>Home Webpage of CSE; Home Webpage of HKUST CSE.</title>
                        </head>
                        
                        <body>
                        This is the home page of HKUST CSE.
                        <a href=../../>Go Home!</a>
                        <a href=/home/cse/coffee>Coffee?</a>
                        <a href=/home/cse/rage>Angry?</a>
                        <a href=/home/cse/nobody>I want nobody nobody but you!</a>
                        </body>
                        """))
                .get("/home/cse/coffee", ctx -> ctx.contentType(ContentType.APPLICATION_YAML)
                        .result("Java is a type of coffee!"))
                .get("/home/cse/rage", ctx -> ctx.contentType(ContentType.APPLICATION_PDF)
                        .result(new byte[]{0xC, 0xA, 0xF, 0xE, 0xB, 0xA, 0xB, 0xE}))
                .get("/home/cse/nobody", ctx -> ctx.contentType(ContentType.APPLICATION_JAR).status(HttpStatus.OK))
                .start();
        Spider spider = new Spider(URI.create("http://localhost:" + app.port() + "/home/cse").toURL(), conn);
        spider.discover(3000);
        Document doc = conn.getDocFromId(0);
        doc.retrieveFromDatabase(conn);
        assertTrue(doc.titleFrequencies().containsValue("cse"));
        assertEquals(2, conn.getDocuments().size());
    }
}
