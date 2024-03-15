package hk.ust.comp4321.db;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal class for operating on tables which represent
 * a word in the title of a document.
 *
 * <p>In particular, the class prepends title_ to each stem.
 */
class TitleTableOperation extends TableOperation {
    private final DSLContext create;
    private static AtomicInteger nextWordId = null;

    TitleTableOperation(DSLContext create) {
        super(create);
        this.create = create;
        if (nextWordId == null) {
            nextWordId = new AtomicInteger(
                    create.fetchCount(DSL.table("WordIndex"),
                            DSL.condition("typePrefix = '" + getPrefix() + "'")));
        }
    }

    @Override
    public String getPrefix() {
        return "title";
    }

    @Override
    public List<String> getStems() {
        return create.meta().getTables()
                .stream().map(Table::getName).filter(s -> s.startsWith("title_")).toList();
    }

    @Override
    public int getNextId() {
        return nextWordId.getAndIncrement();
    }
}
