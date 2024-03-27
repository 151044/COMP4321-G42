package hk.ust.comp4321.spider;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Long.parseLong;

/**
 * A class used to discover web pages.
 */
public class Spider {
    private final URL base;
    private final DatabaseConnection conn;
    private int indexed = 0;
    // private int recurDepth = 0;
    private final SimpleDateFormat headerDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

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
     * Attempts to discover web pages from the specified URL.
     * @param type The stop condition to use
     * @param threshold The integer threshold to stop crawling at
     * @return The List of discovered URLs
     */
    public List<URL> discover(StopType type, int threshold) {
        return discover(base, type, threshold);
    }
    private List<URL> discover(URL url, StopType type, int threshold) {

        // For BFS purposes
        // HashSet and List separate to retain insertion order
        HashSet<URL> visitedLinks = new HashSet<>();
        List<URL> retLinks = new ArrayList<>();
        LinkedList<URL> queue = new LinkedList<>();
        LinkedList<Integer> parentIDs = new LinkedList<>();

        // start
        retLinks.add(url);
        queue.add(url);
        visitedLinks.add(url);
        indexed++;

        while (indexed < threshold) {

            if (queue.isEmpty()) {
                break;
            }
            // get current url
            URL currentURL = queue.removeFirst();

            try {
                Connection.Response response = Jsoup.connect(currentURL.toString()).execute();

                if (response.statusCode() == 200) {
                    if (!parentIDs.isEmpty()) {
                        retLinks.add(currentURL);
                        conn.insertLink(parentIDs.removeFirst(), currentURL);
                        indexed++;
                    }
                } else {
                    continue;
                }

                Instant lastModifiedDate = headerDate.parse(response.header("Last-Modified")).toInstant();

                try {
                    Document currDoc = conn.getDocFromUrl(currentURL);
                    if (lastModifiedDate.isAfter(currDoc.lastModified())) {
                        Document doc = new Document(
                                currentURL,
                                currDoc.id(),
                                lastModifiedDate,
                                parseLong(response.header("Content-Length"))
                        );
                        doc.retrieveFromWeb();
                        conn.insertDocument(doc);

                        Elements links = response.parse().select("a[href]");
                        for (Element link : links) {
                            URL linkURL = new URL(link.attr("abs:href"));
                            if (!visitedLinks.contains(linkURL)) {
                                visitedLinks.add(linkURL);
                                queue.add(linkURL);
                                parentIDs.add(currDoc.id());
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    int nextID = DatabaseConnection.nextDocId();
                    Document doc = new Document(
                            currentURL,
                            nextID,
                            lastModifiedDate,
                            parseLong(response.header("Content-Length"))
                    );
                    doc.retrieveFromWeb();
                    doc.writeWords(conn);

                    Elements links = response.parse().select("a[href]");
                    for (Element link : links) {
                        URL linkURL = new URL(link.attr("abs:href"));
                        if (!visitedLinks.contains(linkURL)) {
                            visitedLinks.add(linkURL);
                            queue.add(linkURL);
                            parentIDs.add(nextID);
                        }
                    }
                }
            }
            catch (Exception e) {
                continue;
            }
        }

        return retLinks;
    }

    /**
     * Represents the type of stopping condition of the spider.
     */
    public enum StopType {
        /**
         * The depth stopping condition.
         * When the spider reaches a certain recursive depth, stop crawling.
         */
        DEPTH,
        /**
         * The indexed stopping condition.
         * When the spider has indexed a certain number of pages, stop crawling.
         */
        INDEXED
    }
}
