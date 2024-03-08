package hk.ust.comp4321.db;

import org.jooq.Table;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;

/**
 * Internal class for operating on tables which represent
 * a word in the title of a document.
 *
 * <p>In particular, the class appends _title to each stem.
 */
class TitleTableOperation extends TableOperation {
    private final Connection conn;

    TitleTableOperation(Connection conn) {
        super(conn);
        this.conn = conn;
    }

    @Override
    public String addSuffix(String stem) {
        return stem + "_title";
    }

    @Override
    public List<String> getStems() {
        return DSL.using(conn).meta().getTables()
                .stream().map(Table::getName).filter(s -> s.endsWith("_title")).toList();
    }
}
