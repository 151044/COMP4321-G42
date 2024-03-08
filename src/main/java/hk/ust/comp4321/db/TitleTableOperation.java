package hk.ust.comp4321.db;

import java.sql.Connection;
import java.util.List;

/**
 * Internal class for operating on tables which represent
 * a word in the title of a document.
 *
 * <p>In particular, the class appends _word to each stem.
 */
class TitleTableOperation extends TableOperation {
    TitleTableOperation(Connection conn) {
        super(conn);
    }

    @Override
    public String addSuffix(String stem) {
        return null;
    }

    @Override
    public List<String> getStems() {
        return null;
    }
}
