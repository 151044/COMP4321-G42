package hk.ust.comp4321.spider;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;

/**
 * A class used to discover web pages.
 */
public class Spider {
    private final URL base;
    private final DatabaseConnection conn;

    /**
     * Constructs a new Spider.
     * @param base The base URL to crawl from
     * @param conn The database connection to use
     */
    public Spider(URL base, DatabaseConnection conn) {
        this.base = base;
        this.conn = conn;
    }
    /**
     * Retrieves the page size through various means.
     * @param response The response to retrieve from
     * @return The size of the page
     */
    private Long retrievePageSize(Connection.Response response) {
        if (response.hasHeader("Size")) {
            return parseLong(response.header("Size"));
        }
        if (response.hasHeader("Content-Length")) {
            return parseLong(response.header("Content-Length"));
        }
        // If there are no headers related to size, count the number of characters on the page
        return (long) response.body().length();
    }

    /**
     * Attempts to discover web pages from the specified URL.
     * @param threshold The integer threshold to stop crawling at
     * @return The List of discovered URLs
     */
    public List<URL> discover(int threshold) throws IOException {
        // Reset indexed
        int indexed = 0;

        // For BFS purposes
        // Update: There is no need to retain insertion order, but retLinks remains to not return visited dead links
        Set<URL> visitedLinks = new HashSet<>();
        List<URL> retLinks = new ArrayList<>();
        Queue<URL> queue = new ArrayDeque<>();

        queue.add(base);

        while (indexed < threshold) {

            if (queue.isEmpty()) {
                break;
            }
            // get current url

            URL currentURL = queue.poll();

            try {
                Connection currConnection = Jsoup.connect(currentURL.toString());
                org.jsoup.nodes.Document jsoupDoc;
                Connection.Response response;

                try {
                    response = currConnection.execute();
                    jsoupDoc = response.parse();
                } catch (IOException e) {
                    System.err.println("Unable to connect to " + currentURL);
                    e.printStackTrace();
                    continue;
                }

                Instant lastModifiedDate = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(response.header(response.hasHeader("Last-Modified") ? "Last-Modified" : "Date")));

                if (conn.hasDocUrl(currentURL)) {
                    Document currDoc = conn.getDocFromUrl(currentURL);
                    currDoc.retrieveFromWeb(jsoupDoc);
                    for (URL link : currDoc.children()) {
                        if (!visitedLinks.contains(link)) {
                            visitedLinks.add(link);
                            queue.add(link);
                            conn.insertLink(currDoc.id(), link);
                        }
                    }
                    if (lastModifiedDate.isAfter(currDoc.lastModified())) {
                        retLinks.add(currentURL);
                        indexed++;
                        Document doc = new Document(
                                currentURL,
                                currDoc.id(),
                                lastModifiedDate,
                                retrievePageSize(response)
                        );
                        conn.insertDocument(doc);
                        conn.deleteFrequencies(doc.id());
                        currDoc.writeWords(conn);
                    }
                } else {
                    retLinks.add(currentURL);
                    indexed++;

                    int nextID = DatabaseConnection.nextDocId();
                    Document doc = new Document(
                            currentURL,
                            nextID,
                            lastModifiedDate,
                            retrievePageSize(response)
                    );
                    doc.retrieveFromWeb(jsoupDoc);
                    doc.writeWords(conn);

                    for (URL link : doc.children()) {
                        if (!visitedLinks.contains(link)) {
                            visitedLinks.add(link);
                            queue.add(link);
                            conn.insertLink(nextID, link);
                        }
                    }
                }
            } catch (ValidationException e) {
                System.err.println("Warning: Unable to crawl " + currentURL + ".");
                e.printStackTrace();
            }
        }
        return retLinks;
    }
    /**
     * The main method.
     * @param args Command-line arguments; currently unused.
     */
    public static void main(String[] args) throws SQLException, IOException {
        // Phase 1 - Spider
        Path phaseOneDb = Path.of("spider_result.db");
        Path phaseOneResult = Path.of("spider_result.txt");

        Files.deleteIfExists(phaseOneDb);

        DatabaseConnection conn = new DatabaseConnection(phaseOneDb);
        Spider spider = new Spider(URI.create("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm").toURL(), conn);
        spider.discover(30);
        writeToFile(phaseOneDb, phaseOneResult, 30);
        conn.close();
    }

    /**
     * Writes the contents of the database to the output file.
     * @param dbPath The path of the database
     * @param outputPath The path to output the file to
     * @param maxSize The number of entries to include
     * @throws SQLException If connecting to the database fails
     * @throws IOException If writing to the file fails
     */
    public static void writeToFile(Path dbPath, Path outputPath, int maxSize) throws SQLException, IOException {
        Files.deleteIfExists(outputPath);
        try (DatabaseConnection conn = new DatabaseConnection(dbPath)) {
            List<Document> docs = conn.getDocuments();
            StringBuilder sb = new StringBuilder();
            docs.stream().limit(maxSize).forEach(d -> {
                try {
                    d.retrieveFromDatabase(conn);
                    sb.append(d.title()).append("\n");
                    sb.append(d.url().toString()).append("\n");
                    sb.append(d.lastModified()).append(", ").append(d.size()).append("\n");
                    Map<String, Long> frequencies = d.bodyFrequencies().entrySet().stream()
                            .map(s -> s.getKey().rawWord().isEmpty() ?
                                    s.getValue() : s.getKey().rawWord())
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
                    sb.append(frequencies.entrySet().stream().sorted(
                                            Map.Entry.<String, Long>comparingByValue().reversed())
                                    .limit(10).map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining("; ")))
                            .append("\n");
                    sb.append(d.children().stream().limit(10).map(URL::toString)
                            .collect(Collectors.joining("\n")));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                sb.append("\n-------------------------------------------------\n");
            });
            Files.write(outputPath, List.of(sb.toString()));
        }
    }
}
