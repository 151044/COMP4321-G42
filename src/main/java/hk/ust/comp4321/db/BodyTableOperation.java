package hk.ust.comp4321.db;

import java.sql.Connection;
import java.util.List;

/**
 * Internal class for operating on tables which represent
 * a word in the body of a document.
 *
 * <p>In particular, the class appends _body to each stem.
 */
class BodyTableOperation extends TableOperation {
    BodyTableOperation(Connection conn) {
        super(conn);
    }

    @Override
    public String addSuffix(String stem) {
        return stem;
    }

    @Override
    public List<String> getStems() {
        return List.of();
    }
}
