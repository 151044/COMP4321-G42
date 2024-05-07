package hk.ust.comp4321.server;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import hk.ust.comp4321.se.SearchEngine;
import hk.ust.comp4321.se.SearchVector;
import hk.ust.comp4321.util.Tuple;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebServer {

    private static final DatabaseConnection conn;

    static {
        try {
            conn = new DatabaseConnection(Path.of("spider_result.db"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String currentPage = getHomepage();

    public static void main(String[] args) throws IOException, SQLException {
        List<Document> docs = conn.getDocuments();
        docs.forEach(d -> {
            try {
                d.retrieveFromDatabase(conn);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        SearchEngine engine = new SearchEngine(conn, docs);
        Javalin app = Javalin.create()
                .get("/", ctx -> ctx.html(getHomepage()))
                .post("/home", ctx -> {
                    currentPage = getHomepage();
                    ctx.html(currentPage);
                })
                .post("/homeSearch", ctx -> {
                    long start = System.currentTimeMillis();
                    String query = ctx.formParam("queryText");

                    if (query == null || query.trim().isEmpty()) {
                        ctx.html(getHomepage());
                        return;
                    }

                    SearchVector vectorQuery = new SearchVector(query);
                    List<Tuple<Document, Double>> search = engine.search(vectorQuery);

                    long end = System.currentTimeMillis();
                    currentPage = getSearchPage(query, search.size(), (double)(end - start) / 1000, search);
                    ctx.html(currentPage);
                })
                .post("/searchSearch", ctx -> {
                    long start = System.currentTimeMillis();
                    String query = ctx.formParam("queryText");

                    if (query == null || query.trim().isEmpty()) {
                        ctx.html(currentPage);
                        return;
                    }

                    SearchVector vectorQuery = new SearchVector(query);
                    List<Tuple<Document, Double>> search = engine.search(vectorQuery);
                    System.out.println(query);
                    System.out.println(search);

                    long end = System.currentTimeMillis();
                    currentPage = getSearchPage(query, search.size(), (double)(end - start) / 1000, search);
                    ctx.html(currentPage);
                })
                .error(404, ctx -> {
                    ctx.html(getErrorPage());
                });
        app.get("/shutdown", ctx -> {
            ctx.html("Shutting down...");
            conn.close();
            app.stop();
        });
        app.start();
    }

    private static String getErrorPage() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Error 404 - COMP 4321 Group 42 Search Engine</title>
                </head>
                %s
                <body>
                %s
                <div style="padding: 10px;">Error! Page not found.</div>
                </body>
                """.formatted(getSearchpageStyle(), getSearchpageHeader());
    }

    private static String getHomepageTitle() {
        return """
                <head>
                    <title>COMP 4321 Group 42 Search Engine</title>
                </head>
               """;
    }

    private static String getHomepageStyle() {
        return """
                <style>
                    body {
                        display: block;
                        justify-content: space-around;
                        align-items: center;
                        font-size: 18px;
                        background-color: #daebf1;
                        font-family: 'Open Sans', sans-serif;
                    }
                    .title {
                        margin: 0;
                        padding-top: 5em;
                        padding-bottom: 1em;
                        text-align: center;
                        color: black;
                    }
                    form {
                        width: 100%;
                        text-align: center;
                    }
                    input {
                        width: 100%;
                        padding: 0.5em;
                        font-size: 1em;
                        border-radius: 6px;
                        height: 50px;
                        text-indent: 0.25em;
                        border: none;
                        outline: solid lightgrey;
                        background-color: rgba(255,255,255,0.7);
                        transition: all 0.2s ease;
                    }
                    input:focus,
                    input:active {
                        outline: none;
                        background-color: rgba(255,255,255,0.9);
                    }
                    .input-container {
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        width: 70%;
                        margin: 0 auto;
                        max-width: 680px;
                    }
                    button {
                        width: 50%;
                        max-width: 150px;
                        color: black;
                        background-color: rgba(220, 220, 220, 1.0);
                        padding: 0.7em;
                        font-size: 1em;
                        border: none;
                        border-radius: 6px;
                        margin: 1em 0;
                        cursor: pointer;
                        transition: all 0.1s ease;
                        box-shadow: 0 5px 5px rgba(0, 0, 0, 0.5);
                        font-family: 'Open Sans', sans-serif;
                    }
                    button:active,
                    button:hover{
                        transform: translateY(5px);
                        box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
                    }
                    button:active,
                    button:focus {
                        outline: none;
                    }
                </style>
               """;
    }

    private static String getHomepage() {
        return """
                <!DOCTYPE html>
                <html>
                %s
                %s
                <body>
                <h1 class="title">COMP 4321 Search Engine</h1>
                <form id="homeSearchForm", action="/homeSearch" method="POST">
                    <div class="input-container">
                        <label for="queryText"></label>
                        <input type="text" name="queryText" id="queryText" placeholder="Search"/>
                    </div>
                    <div>
                        <button class="searchButton">Search</button>
                    </div>
                </form>
                </body>
               """.formatted(getHomepageTitle(), getHomepageStyle());
    }

    private static String getSearchpageTitle(String query) {
        return """
                <head>
                    <title> %s - COMP 4321 Group 42 Search Engine</title>
                </head>
               """.formatted(query);
    }

    private static String getSearchpageStyle() {
        return """
                <style>
                body {
                    display: block;
                    justify-content: space-around;
                    align-items: center;
                    font-size: 18px;
                    background-color: #daebf1;
                    font-family: 'Open Sans', sans-serif;
                }
                form {
                    width: 100%;
                    text-align: center;
                }
                input {
                    width: 20em;
                    padding: 0.5em;
                    font-size: 1em;
                    border-radius: 6px;
                    height: 25px;
                    text-indent: 0.25em;
                    border: none;
                    outline: solid lightgrey;
                    background-color: rgba(255,255,255,0.7);
                    transition: all 0.2s ease;
                }
                input:focus,
                input:active {
                    outline: none;
                    background-color: rgba(255,255,255,0.9);
                }
                .input-container {
                    display: flex;
                    justify-content: left;
                    align-items: center;
                    width: 200%;
                    margin: 0 auto;
                    max-width: 680px;
                }
                .homeButton {
                    width: 8em;
                    height: 45px;
                    color: black;
                    background-color: #daebf1;
                    padding: 0.3em;
                    font-size: 1.5em;
                    font-weight: bold;
                    border: none;
                    border-radius: 6px;
                    margin-left: 0;
                    cursor: pointer;
                    font-family: 'Open Sans', sans-serif;
                    text-align: left;
                }
                .searchButton {
                    width: 6em;
                    height: 45px;
                    color: black;
                    background-color: rgba(220, 220, 220, 1.0);
                    padding: 0.5em;
                    font-size: 1em;
                    border: none;
                    border-radius: 6px;
                    margin-left: 5px;
                    cursor: pointer;
                    transition: all 0.1s ease;
                    font-family: 'Open Sans', sans-serif;
                }
                .searchButton:active,
                .searchButton:hover{
                    background-color: rgba(200, 200, 200, 1.0);
                }
                .searchButton:active,
                .searchButton:focus {
                    outline: none;
                }
                .header {
                    color:black;
                    overflow: hidden;
                    padding: 20px;
                    text-align: center;
                    border-style: none;
                    font-family: 'Open Sans', sans-serif;
                    text-decoration: none
                }
                .searchResultTitle {
                    color: black;
                    font-size: 25px;
                    font-weight: bold;
                    padding: 10px;
                    margin-top: 5px;
                }
                .searchResultInfo {
                    color: rgb(100, 100, 100);
                    font-size: 15px;
                    padding: 10px;
                    margin-top: 0;
                }
                .searchResultItem {
                    padding: 5px;
                    margin-top: 5px;
                }
                .searchResultItemText {
                    font-weight: normal;
                    text-align: left;
                }
                .searchResultPageScore {
                    width: 75px;
                    font-weight: normal;
                    text-align: left;
                    padding-top: 5px;
                }
                .searchResultPageTitle {
                    color: black;
                    font-size: 25px;
                    font-weight: bold;
                    text-align: left;
                }
                .searchResultPageLink,
                .searchResultPageInfo,
                .searchResultPageWordFreq {
                    font-weight: normal;
                    text-align: left;
                }
                .searchResultPageTitle,
                .searchResultPageLink,
                .searchResultPageInfo,
                .searchResultPageWordFreq,
                .searchResultItemText {
                    padding-left: 10px;
                }
                </style>
              """;
    }

    private static String getSearchpageHeader() {
        return """
                <div id="header">
                    <table>
                        <tr>
                            <th>
                                <form action="/home" method="POST">
                                <button class="homeButton">Search Engine</button>
                                </form>
                            </th>
                            <th>
                                <form action="/searchSearch" method="POST">
                                    <div class="input-container">
                                        <label for="queryText"></label>
                                        <input type="text" name="queryText" id="queryText" placeholder="Search"/>
                                        <button class="searchButton">Search</button>
                                    </div>
                                </form>
                            </th>
                        </tr>
                    </table>
                </div>
               """;
    }

    private static String getSearchResultTitle(String query) {
        return """
               <div class="searchResultTitle">Search results for <span style="font-style:italic">%s</span></div>
               """.formatted(query);
    }

    private static String getSearchResultInfo(int n, double t) {
        return """
                <div class="searchResultInfo">%d results (%f seconds)</div>
               """.formatted(n, t);
    }

    private static String getSearchResultPageItem(double score, Document doc) {
        String pageURL = doc.url().toString();
        String lastModified = LocalDate.ofInstant(doc.lastModified(), ZoneOffset.UTC).toString();
        Map<String, Long> frequencies =
                Stream.concat(doc.bodyFrequencies().entrySet().stream(),
                                doc.titleFrequencies().entrySet().stream())
                        .map(s -> s.getKey().rawWord().isEmpty() ?
                                s.getValue() : s.getKey().rawWord())
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        String keyWords = frequencies.entrySet().stream().sorted(
                                    Map.Entry.<String, Long>comparingByValue().reversed())
                                    .limit(10).map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining("; "));
        String parentLinks = conn.parents(doc.id()).stream()
                                                    .map(x -> x.url().toString())
                                                    .map(x -> "<div class=\"searchResultPageLink\"><a href=\"%s\">%s</a></div>".formatted(x,x))
                                                    .collect(Collectors.joining());
        String childLinks = conn.children(doc.id()).stream()
                                                    .map(URL::toString)
                                                    .map(x -> "<div class=\"searchResultPageLink\"><a href=\"%s\">%s</a></div>".formatted(x,x))
                                                    .collect(Collectors.joining());
        return """
                <div class="searchResultItem">
                  <table>
                    <tr>
                      <th style="vertical-align: top;">
                      <div class="searchResultPageScore">%f</div>
                      </th>
                      <th>
                      <div class="searchResultPageTitle">%s</div>
                      <div class="searchResultPageLink"><a href="%s">%s</a></div>
                      <div class="searchResultPageInfo">Last modified on %s; Page size: %d</div>
                      <div class="searchResultPageWordFreq">%s</div>
                      <br>
                      <div class="searchResultItemText">Parent links:</div>
                      %s
                      <br>
                      <div class="searchResultItemText">Child links:</div>
                      %s
                      </th>
                    </tr>
                   </table>
                </div>
               """.formatted(score, doc.title(), pageURL, pageURL, lastModified, doc.size(), keyWords, parentLinks, childLinks);
    }

    private static String getSearchResultPages(List<Tuple<Document, Double>> docs) {
        return docs.stream().map(d -> getSearchResultPageItem(d.right(), d.left())).collect(Collectors.joining("\n"));
    }

    private static String getSearchPage(String query, int n, double t, List<Tuple<Document, Double>> docs) {
        return """
                <!DOCTYPE html>
                <html>
                %s
                %s
                <body>
                %s
                %s
                %s
                %s
                </body>
               """.formatted(getSearchpageTitle(query),
                            getSearchpageStyle(),
                            getSearchpageHeader(),
                            getSearchResultTitle(query),
                            getSearchResultInfo(n, t),
                            getSearchResultPages(docs));
    }
}
