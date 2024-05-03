package hk.ust.comp4321.db;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordInfo;
import hk.ust.comp4321.test.ReflectUtil;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
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
        ReflectUtil.setStaticField("nextWordId", null, BodyTableOperation.class);
        ReflectUtil.setStaticField("nextWordId", null, TitleTableOperation.class);
    }

    public static DatabaseConnection initializeTestDb() throws SQLException, NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
        Path testPath = Path.of("test.db");
        Files.deleteIfExists(testPath);
        resetId();
        DatabaseConnection conn = new DatabaseConnection(testPath);
        Connection connect = conn.getConnection();
        DSLContext create = DSL.using(connect);
        List<String> tableNames = List.of("body_0", "title_0", "title_1", "body_1", "body_2", "body_3", "title_2");
        tableNames.forEach(s -> create.createTableIfNotExists(s)
                .column("docId", INTEGER)
                .column("paragraph", INTEGER)
                .column("sentence", INTEGER)
                .column("location", INTEGER)
                .column("suffix", VARCHAR)
                .execute());

        List<DbUtil.WordIndexEntry> wordIndices = List.of(
                new WordIndexEntry("comput", 0, "body"),
                new WordIndexEntry("comput", 0, "title"),
                new WordIndexEntry("locat", 1, "body"),
                new WordIndexEntry("locat", 1, "title"),
                new WordIndexEntry("engin", 2, "body"),
                new WordIndexEntry("educ", 3, "body"),
                new WordIndexEntry("opportun", 2, "title")
        );
        wordIndices.forEach(entry -> create.insertInto(DSL.table("WordIndex"))
                .values(entry.stem(), entry.id(), entry.prefix())
                .execute());

        List<WordInfo> computEntries = List.of(
                new WordInfo(0, 1, 1, 1, "computing"),
                new WordInfo(0, 1, 2, 3, "compute"),
                new WordInfo(0, 99, 2, 3, "computed"),
                new WordInfo(1, 3, 2, 1, "computes"),
                new WordInfo(1, 3270972, 2, 1, "computer"));
        computEntries.forEach(info -> create.insertInto(DSL.table("body_0"))
                .values(info.docId(), info.paragraph(), info.sentence(), info.wordLocation(), info.rawWord())
                .execute());

        List<WordInfo> computTitles = List.of(
                new WordInfo(0, 1, 1, 1, "computing"),
                new WordInfo(1, 1, 1, 1, "computers"));
        computTitles.forEach(info -> create.insertInto(DSL.table("title_0"))
                .values(info.docId(), info.paragraph(), info.sentence(), info.wordLocation(), info.rawWord())
                .execute());

        List<Document> docs = List.of(
                new Document(new URI("https://www.cse.ust.hk/~kwtleung/").toURL(), 0, Instant.ofEpochMilli(1709693690504L), 25565, "Kenneth Wai-Ting LEUNG (PhD, HKUST, 2010)"),
                new Document(new URI("https://www.w3schools.com/sql/sql_insert.asp").toURL(), 1, Instant.ofEpochMilli(1709693690504L), 25565, "SQL INSERT INTO Statement"),
                new Document(new URI("https://sqlite.org/lang_datefunc.html").toURL(), 2, Instant.ofEpochMilli(93690504), 2639425, "Date And Time Functions"),
                new Document(new URI("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/package-summary.html").toURL(), 3, Instant.ofEpochMilli(95023232344L), 263942533, "java.time (Java SE 17 & JDK 17)"),
                new Document(new URI("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.html#currentTimeMillis()").toURL(), 4, Instant.ofEpochMilli(93690504), 2639425, "System (Java SE 17 & JDK 17)")
        );

        docs.forEach(doc -> create.insertInto(DSL.table("Document"))
                .values(doc.url().toString(), doc.id(), doc.lastModified(), doc.size(), doc.title())
                .execute());

        List<List<Integer>> links = List.of(List.of(0, 1), List.of(0, 2), List.of(0, 3), List.of(0, 4),
                List.of(4, 0), List.of(4, 2), List.of(3, 1), List.of(3, 3));
        links.forEach(l -> create.insertInto(DSL.table("DocumentLink"))
                .values(l.get(0), docs.get(l.get(1)).url().toString()).execute());

        List<ForwardIndexEntry> forwardEntries = List.of(
                new ForwardIndexEntry(0, 0, "title"),
                new ForwardIndexEntry(0, 0, "body"),
                new ForwardIndexEntry(0, 1, "body"),
                new ForwardIndexEntry(1, 0, "body")
        );

        forwardEntries.forEach(e -> create.insertInto(DSL.table("ForwardIndex"))
                .values(e.docId, e.wordId, e.prefix).execute());


        conn.close();

        /*
         * Note: Since we bypassed all proper APIs to insert Documents into the database,
         * the Doc IDs are wrong. We close and reopen the connection to fix this problem.
         */
        resetId();
        conn = new DatabaseConnection(testPath);
        return conn;
    }
    public record WordIndexEntry(String stem, int id, String prefix) {}
    public record ForwardIndexEntry(int docId, int wordId, String prefix) {}
}
