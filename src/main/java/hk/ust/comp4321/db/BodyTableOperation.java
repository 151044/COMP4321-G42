package hk.ust.comp4321.db;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal class for operating on tables which represent
 * a word in the body of a document.
 *
 * <p>In particular, the class prepends body_ to each stem.
 */
class BodyTableOperation extends TableOperation {
    private final DSLContext create;
    private static AtomicInteger nextWordId = null;

    BodyTableOperation(DSLContext create) {
        super(create);
        this.create = create;
        if (nextWordId == null) {
            nextWordId = new AtomicInteger(
                    create.fetchCount(DSL.table(DSL.name("WordIndex")),
                            DSL.condition(DSL.field(DSL.name("typePrefix")).eq(getPrefix()))));
        }
    }

    @Override
    public String getPrefix() {
        return "body";
    }

    @Override
    public int getNextId() {
        return nextWordId.getAndIncrement();
    }

    @Override
    public int getCurrentId() {
        return nextWordId.get();
    }
}