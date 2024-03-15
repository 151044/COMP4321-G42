package hk.ust.comp4321.test;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordInfo;
import hk.ust.comp4321.db.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

public class DbUtil {
    private static void resetId() throws NoSuchFieldException, IllegalAccessException {
        ReflectUtil.setStaticField("nextDocId", null, DatabaseConnection.class);
    }

    private static DatabaseConnection conn;
    public static DatabaseConnection initializeTestDb() throws SQLException, NoSuchFieldException, IllegalAccessException, URISyntaxException, MalformedURLException {
        Path testPath = Path.of("test.db");
        resetId();
        conn = new DatabaseConnection(testPath);
        Connection connect = conn.getConnection();
        DSLContext create = DSL.using(connect);
        List<String> tableNames = List.of("body_0", "title_0", "title_1", "body_1");
        tableNames.forEach(s -> create.createTableIfNotExists(s)
                .column("docId", INTEGER)
                .column("paragraph", INTEGER)
                .column("sentence", INTEGER)
                .column("location", INTEGER)
                .column("suffix", VARCHAR)
                .execute());

        create.createTableIfNotExists("WordIndex")
                .column("stem", VARCHAR)
                .column("wordId", INTEGER)
                .column("typeSuffix", VARCHAR)
                .constraints(
                        DSL.primaryKey("wordId", "typeSuffix")
                )
                .execute();

        List<DbUtil.WordIndexEntry> wordIndices = List.of(
                new DbUtil.WordIndexEntry("Comput", 0, "body"),
                new DbUtil.WordIndexEntry("Comput", 0, "title"),
                new DbUtil.WordIndexEntry("Locat", 1, "body"),
                new DbUtil.WordIndexEntry("Locat", 1, "title")
        );
        wordIndices.forEach(entry -> create.insertInto(DSL.table("WordIndex"))
                .values(entry.stem(), entry.id(), entry.suffix())
                .execute());

        List<WordInfo> computEntries = List.of(
                new WordInfo(0, 1, 1, 1, "ing"),
                new WordInfo(0, 1, 2, 3, "e"),
                new WordInfo(0, 99, 2, 3, "ed"),
                new WordInfo(1, 3, 2, 1, "es"),
                new WordInfo(1, 3270972, 2, 1, "er"));
        computEntries.forEach(info -> create.insertInto(DSL.table("body_0"))
                .values(info.docId(), info.paragraph(), info.sentence(), info.wordLocation(), info.suffix())
                .execute());

        List<WordInfo> computTitles = List.of(
                new WordInfo(0, 1, 1, 1, "ing"),
                new WordInfo(1, 1, 1, 1, "ers"));
        computTitles.forEach(info -> create.insertInto(DSL.table("title_0"))
                .values(info.docId(), info.paragraph(), info.sentence(), info.wordLocation(), info.suffix())
                .execute());

        List<Document> docs = List.of(
                new Document(new URI("https://www.cse.ust.hk/~kwtleung/").toURL(), 0, Instant.ofEpochMilli(1709693690504L), 25565),
                new Document(new URI("https://www.w3schools.com/sql/sql_insert.asp").toURL(), 1, Instant.ofEpochMilli(1709693690504L), 25565),
                new Document(new URI("https://sqlite.org/lang_datefunc.html").toURL(), 2, Instant.ofEpochMilli(93690504), 2639425),
                new Document(new URI("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/package-summary.html").toURL(), 3, Instant.ofEpochMilli(95023232344L), 263942533),
                new Document(new URI("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#currentTimeMillis()").toURL(), 4, Instant.ofEpochMilli(93690504), 2639425)
        );

        docs.forEach(doc -> create.insertInto(DSL.table("Document"))
                .values(doc.url().toString(), doc.id(), doc.lastModified(), doc.size())
                .execute());

        List<List<Integer>> links = List.of(List.of(0, 1), List.of(0, 2), List.of(0, 3), List.of(0, 4),
                List.of(4, 0), List.of(4, 2), List.of(3, 1), List.of(3, 3));
        links.forEach(l -> create.insertInto(DSL.table("DocumentLink"))
                .values(l.get(0), l.get(1)).execute());

        conn.close();

        /*
         * Note: Since we bypassed all proper APIs to insert Documents into the database,
         * the Doc IDs are wrong. We close and reopen the connection to fix this problem.
         */
        resetId();
        conn = new DatabaseConnection(testPath);
        return conn;
    }
    public record WordIndexEntry(String stem, int id, String suffix) {}
}
