package hk.ust.comp4321.db;

import hk.ust.comp4321.api.WordInfo;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

/**
 * Encapsulates operations on a group (or a type) of tables.
 *
 * <p>The class defines a common set of operations on
 * a group of tables. The tables are normally based on
 * the ID of the stem, and prefixes`body`, followed by `body`. are added to the stem by the
 * {@link #getPrefix(int)} method. The class operates
 * on all tables with the prefix as a group; this allows
 * for more efficient manipulation and code reuse.
 */
public abstract class TableOperation {
    private final DSLContext create;

    TableOperation(DSLContext create) {
        this.create = create;
    }

    /**
     * Gets the prefix that this type of table operates on, without underscores.
     * @return The prefix of this kind of table operation
     */
    public abstract String getPrefix();
    /**
     * Gets all the table names associated with this kind of database.
     * @return The list of raw table names in the database satisfying some criteria
     */
    public abstract List<String> getTableNames();

    /**
     * Gets the word IDs associated with this prefix.
     * @return The list of word IDs with the prefix
     */
    public List<Integer> getStemIds() {
        return getTableNames().stream().map(s -> s.replace(getPrefix(), "")).map(Integer::parseInt).toList();
    }

    /**
     * Gets the next word ID for this prefix.
     *
     * @implNote It is expected that subclasses will use static
     * fields to maintain consistency for IDs. Hence, this method
     * is abstract in order to allow for the same ID with different
     * prefixes.
     * @return The next word ID to allocate for this prefix
     */
    public abstract int getNextId();

    /**
     * Transforms a table name into the prefixed form.
     * @param stem The word ID of the stem to transform
     * @return The prefixed string representing a table name in the database
     */
    public String getPrefix(int stem) {
        return getPrefix() + "_" + stem;
    }

    /**
     * Inserts word information associated with a word ID into the corresponding table of the database.
     * @param stem The ID of the stemmed word to insert
     * @param freq The word frequency record to associate with this word
     */
    public void insertWordInfo(int stem, WordInfo freq) {
        String tableName = getPrefix(stem);
        create.insertInto(DSL.table(DSL.name(tableName)))
                .values(freq.docId(), freq.paragraph(), freq.sentence(), freq.wordLocation(), freq.suffix())
                .onDuplicateKeyIgnore()
                .execute();
    }
    /**
     * Finds the corresponding title word frequencies of the stem in this kind of table only.
     *
     * @param stem The word ID representing the stem to find the word frequencies for
     * @return The list of word frequencies associated with this stem, or an empty
     * list if the word does not exist in the database
     */
    public List<WordInfo> getFrequency(int stem) {
        if (create.meta().getTables(getPrefix(stem)).isEmpty()) {
            return List.of();
        } else {
            return create.select()
                    .from(DSL.table(DSL.name(getPrefix(stem))))
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
        getTableNames().forEach(stem -> create.delete(DSL.table(stem))
                .where(DSL.condition(DSL.field(DSL.name("docId")).eq(docId)))
                .execute());
    }

    /**
     * Retrieves the word ID for the input stem.
     * Returns -1 if no such stem exists in the database with the correct prefix type.
     * @param stem The stem to retrieve the ID for
     * @return The word ID for the stem; -1 if the stem does not exist
     */
    public int getIdFromStem(String stem) {
        return create.select(DSL.field(DSL.name("wordId"))).from(DSL.table(DSL.name("WordIndex")))
                .where(DSL.condition(DSL.field(DSL.name("stem")).eq(stem))
                        .and(DSL.field(DSL.name("typePrefix")).eq(getPrefix())))
                .fetch()
                .stream().findFirst() // This does not use their map since I do not know what it does on an empty list
                .map(r -> r.get(0, Integer.class))
                .orElse(-1);
    }

    /**
     * Gets the stem corresponding to the word ID.
     * @throws IllegalArgumentException If the word ID does not exist with this type
     * @param id The word ID to lookup
     * @return The corresponding stem
     */
    public String getStemFromId(int id) {
        return create.select(DSL.field(DSL.name("stem"))).from(DSL.table(DSL.name("WordIndex")))
                .where(DSL.condition(DSL.field(DSL.name("wordId")).eq(id))
                        .and(DSL.field(DSL.name("typePrefix")).eq(getPrefix())))
                .fetch()
                .stream().findFirst()
                .map(r -> r.get(0, String.class))
                .orElseThrow(() -> new IllegalArgumentException("No such word ID: " + id));
    }

    /**
     * Inserts the stem into the database if it does not exist.
     * Does nothing if the stem already exists.
     *
     * <p>The typical workflow is as follows:
     * <ol>
     *     <li>Check if the word ID corresponding to the word
     *          exists, and allocates a new one if needed.</li>
     *     <li>Update the table with the word frequency.</li>
     *     <li>If the table does not exist, create it.</li>
     * </ol>
     * @param stem The stem to attempt to insert into the database
     * @return The word ID of the inserted stem; or the current word
     * ID of this stem if it already exists
     */
    public int insertStem(String stem) {
        return create.select(DSL.field(DSL.name("wordId"))).from(DSL.table(DSL.name("WordIndex")))
                .where(DSL.condition(DSL.field(DSL.name("stem")).eq(stem)
                        .and(DSL.field(DSL.name("typePrefix")).eq(getPrefix()))))
                .fetch()
                .stream().findFirst()
                .map(r -> r.get(0, Integer.class))
                .orElseGet(() -> {
                    int next = getNextId();
                    create.insertInto(DSL.table(DSL.name("WordIndex")))
                            .values(stem, next, getPrefix())
                            .execute();
                    create.createTableIfNotExists(getPrefix(next))
                            .column("docId", INTEGER)
                            .column("paragraph", INTEGER)
                            .column("sentence", INTEGER)
                            .column("location", INTEGER)
                            .column("prefix", VARCHAR)
                            .constraints(
                                    DSL.primaryKey("docId", "paragraph", "sentence", "location"),
                                    DSL.foreignKey("docId").references("Document", "docId")
                            ).execute();
                    return next;
                });
    }
}
