package hk.ust.comp4321.db;

import hk.ust.comp4321.api.WordFrequency;

import java.sql.Connection;
import java.util.List;
/**
 * Encapsulates operations on a group (or a type) of tables.
 *
 * <p>The class defines a common set of operations on
 * a group of tables. The tables are normally based on
 * the stem, and suffixes are added to the stem by the
 * {@link #addSuffix(String)} method. The class operates
 * on all tables with the suffix as a group; this allows
 * for more efficient manipulation and code reuse.
 */
public abstract class TableOperation {
    private final Connection conn;

    TableOperation(Connection conn) {
        this.conn = conn;
    }

    /**
     * Transforms a table name into the suffixed form.
     * @param stem The stem of the word to transform
     * @return The suffixed string representing a table name in the database
     */
    public abstract String addSuffix(String stem);

    /**
     * Gets all the stems associated with this kind of database.
     * @return The list of raw table names in the database satisfying some criteria
     */
    public abstract List<String> getStems();

    /**
     * Inserts a word into the corresponding table of the database.
     *
     * <p>The typical workflow is as follows:
     * <ol>
     *     <li>Check if the title table corresponding to the word
     *          exists, and create it if needed.</li>
     *     <li>Update the table with the word frequency.</li>
     * </ol>
     * @param stem The stemmed word to insert
     * @param freq The word frequency record to associate with this word
     */
    void insertWord(String stem, WordFrequency freq) {

    }
    /**
     * Finds the corresponding title word frequencies of the stem in this kind of table only.
     *
     * @param stem The stem to find the word frequencies for
     * @return The list of word frequencies associated with this stem, or an empty
     * list if the word does not exist in the database
     */
    List<WordFrequency> getFrequency(String stem) {
        return List.of();
    }
}
