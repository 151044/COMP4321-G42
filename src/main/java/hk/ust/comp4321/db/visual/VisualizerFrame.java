package hk.ust.comp4321.db.visual;

import hk.ust.comp4321.db.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.Table;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * The main frame to show the database tables with.
 */
public class VisualizerFrame extends JFrame {
    private static final List<String> SPECIAL_TABLES = List.of("Document", "DocumentLink", "WordIndex");
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
        SPECIAL_TABLES.forEach(s -> pane.addTab(s,
                new TablePanel(create, tables.stream()
                        .filter(t -> t.getName().equals(s))
                        .findFirst().orElseThrow())));
        pane.addTab("TableLookup", new TableSelectorPanel(create, tables, conn));
        add(pane, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }
}
