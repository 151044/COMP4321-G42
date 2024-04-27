package hk.ust.comp4321.server;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordInfo;
import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.db.TableOperation;
import hk.ust.comp4321.nlp.NltkPorter;
import io.javalin.Javalin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WebServer {
    public static void main(String[] args) throws IOException, SQLException {
        String indexLines = Files.readString(Path.of("src/main/html/index.html"));
        DatabaseConnection conn = new DatabaseConnection(Path.of("spider_result_large.db"));
        Javalin app = Javalin.create()
                .get("/", ctx -> {
                    ctx.html(indexLines);
                })
                .post("/search", ctx -> {
                    String terms = ctx.formParam("word");
                    terms = NltkPorter.stem(terms);
                    TableOperation op = conn.bodyOperator();
                    int wordId = op.getIdFromStem(terms);
                    List<WordInfo> wordInfo = op.getFrequency(wordId);;
                    ctx.html(getQueryAnswer(wordInfo.stream().map(WordInfo::docId).distinct()
                            .map(conn::getDocFromId)
                            .toList(), terms));
                });
        app.get("/shutdown", ctx -> {
            ctx.html("Shutting down...");
            conn.close();
            app.stop();
        });
        app.start();
    }

    private static String getQueryAnswer(List<Document> doc, String term) {
        String result = doc.stream().map(d -> "<h2>" + d.title() + "</h2><br><a href=" + d.url() + ">" + d.url() + "</a>")
                .collect(Collectors.joining());
        return
"""
<!DOCTYPE html>
<html>
<head>
    <title>Search Results for %s</title>
</head>

<body>
<h1>Search Results for %s:</h1>
%s
</body>
""".formatted(term, term, result);
    }
}
