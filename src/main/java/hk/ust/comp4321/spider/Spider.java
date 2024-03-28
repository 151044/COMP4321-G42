package hk.ust.comp4321.spider;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        return discover(base, threshold);
    }
    private List<URL> discover(URL url, int threshold) throws IOException {

        // Reset indexed
        int indexed = 0;

        // For BFS purposes
        // Update: There is no need to retain insertion order, but retLinks remains to not return visited dead links
        Set<URL> visitedLinks = new HashSet<>();
        List<URL> retLinks = new ArrayList<>();
        Queue<URL> queue = new ArrayDeque<>();

        queue.add(url);

        while (indexed < threshold) {

            if (queue.isEmpty()) {
                break;
            }
            // get current url

            URL currentURL = queue.poll();

            try {
                Connection currConnection = Jsoup.connect(currentURL.toString());

                try {
                    currConnection.execute();
                } catch (IOException e) {
                    continue;
                }

                Connection.Response response = currConnection.execute();

                Instant lastModifiedDate = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(response.header(response.hasHeader("Last-Modified") ? "Last-Modified" : "Date")));

                if (conn.hasDocUrl(currentURL)) {
                    Document currDoc = conn.getDocFromUrl(currentURL);
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

                    }
                    currDoc.retrieveFromWeb();
                    for (URL link : currDoc.children()) {
                        if (!visitedLinks.contains(link)) {
                            visitedLinks.add(link);
                            queue.add(link);
                            conn.insertLink(currDoc.id(), link);
                        }
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
                    doc.retrieveFromWeb();
                    doc.writeWords(conn);

                    for (URL link : doc.children()) {
                        if (!visitedLinks.contains(link)) {
                            visitedLinks.add(link);
                            queue.add(link);
                            conn.insertLink(nextID, link);
                        }
                    }
                }
            } catch (HttpStatusException | ValidationException e) {
                continue;
            }
        }
        return retLinks;
    }
}
