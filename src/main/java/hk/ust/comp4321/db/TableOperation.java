package hk.ust.comp4321.db;

import hk.ust.comp4321.api.WordInfo;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

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
    private final DSLContext create;

    TableOperation(Connection conn) {
        create = DSL.using(conn, SQLDialect.SQLITE);
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
    public void insertWord(String stem, WordInfo freq) {
        String tableName = addSuffix(stem);
        create.createTableIfNotExists(tableName)
                .column("docId", INTEGER)
                .column("paragraph", INTEGER)
                .column("sentence", INTEGER)
                .column("location", INTEGER)
                .column("suffix", VARCHAR)
                .constraints(
                        DSL.primaryKey("docId", "paragraph", "sentence", "location"),
                        DSL.foreignKey("docId").references("Document", "docId")
                ).execute();
        create.insertInto(DSL.table(tableName))
                .values(freq.docId(), freq.paragraph(), freq.sentence(), freq.wordLocation())
                .onDuplicateKeyIgnore()
                .execute();
    }
    /**
     * Finds the corresponding title word frequencies of the stem in this kind of table only.
     *
     * @param stem The stem to find the word frequencies for
     * @return The list of word frequencies associated with this stem, or an empty
     * list if the word does not exist in the database
     */
    public List<WordInfo> getFrequency(String stem) {
        if (create.meta().getTables(addSuffix(stem)).isEmpty()) {
            return List.of();
        } else {
            return create.select()
                    .from(DSL.table(addSuffix(stem)))
                    .fetch()
                    .stream().map(r -> new WordInfo(r.get(0, Integer.class), r.get(1, Integer.class),
                            r.get(2, Integer.class), r.get(3, Integer.class), r.get(4, String.class)))
                    .toList();
        }
    }

    /**
     * Deletes all word frequencies in this table operation group associated
     * with the given document ID.
     * @param docId The document IDs to drop
     */
    public void deleteFrequencies(int docId) {
        getStems().forEach(stem -> create.delete(DSL.table(stem))
                .where(DSL.condition("docId = " + docId))
                .execute());
    }
}
