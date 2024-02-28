package hk.ust.comp4321.spider;

import org.jsoup.Jsoup;

import java.net.URL;
import java.util.List;

/**
 * A class used to discover web pages.
 */
public class Spider {
    private int indexed = 0;
    private int recurDepth = 0;

    /**
     * Constructs a new Spider.
     * @param jsoup The JSoup instance to read web pages with
     * @param base The base URL to crawl from
     */
    public Spider(Jsoup jsoup, URL base) {

    }

    /**
     * Attempts to discover web pages from the specified URL.
     * @param type The stop condition to use
     * @param threshold The integer threshold to stop crawling at
     * @return The List of discovered URLs
     */
    public List<URL> discover(StopType type, int threshold) {
        return List.of();
    }
    private List<URL> discover(URL url, StopType type, int threshold) {
        return List.of();
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
