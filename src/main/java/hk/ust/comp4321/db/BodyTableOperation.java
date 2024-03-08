package hk.ust.comp4321.db;

import org.jooq.Named;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;

/**
 * Internal class for operating on tables which represent
 * a word in the body of a document.
 *
 * <p>In particular, the class appends _body to each stem.
 */
class BodyTableOperation extends TableOperation {
    private final Connection conn;

    BodyTableOperation(Connection conn) {
        super(conn);
        this.conn = conn;
    }

    @Override
    public String addSuffix(String stem) {
        return stem + "_body";
    }

    @Override
    public List<String> getStems() {
        return DSL.using(conn).meta().getTables().stream().map(Named::getName)
                .filter(n -> n.endsWith("_body")).toList();
    }
}
