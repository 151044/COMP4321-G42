package hk.ust.comp4321.db.visual;

import hk.ust.comp4321.db.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.List;

/**
 * The main frame to show the database tables with.
 */
public class VisualizerFrame extends JFrame {
    private static final List<TypedTable> SPECIAL_TABLES = List.of(
            new TypedTable("Document", List.of(String.class, Integer.class, Instant.class, Long.class),
                    List.of("url", "docId", "lastModified", "size")),
            new TypedTable("DocumentLink", List.of(Integer.class, String.class),
                    List.of("docId", "childUrl")),
            new TypedTable("WordIndex", List.of(String.class, Integer.class, String.class),
                    List.of("stem", "wordId", "typePrefix")));
    /**
     * Creates a new VisualizerFrame to display database tables.
     * @param create The DSLContext to run SQL queries with
     * @param tables The tables to display
     * @param conn The database connection to lookup IDs with
     */
    public VisualizerFrame(DSLContext create, List<Table<?>> tables, DatabaseConnection conn) {
        super("Database Visualizer");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane pane = new JTabbedPane();
        SPECIAL_TABLES.forEach(s -> pane.addTab(s.name(),
                new TablePanel(create, DSL.table(s.name()), s.types(), s.names())));
        pane.addTab("TableLookup", new TableSelectorPanel(create, tables, conn));
        add(pane, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }
    private record TypedTable(String name, List<Class<?>> types, List<String> names) {}
}
