package hk.ust.comp4321.db.visual;

import org.jooq.DSLContext;
import org.jooq.Table;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * The main frame to show the database tables with.
 */
public class VisualizerFrame extends JFrame {
    /**
     * Creates a new VisualizerFrame to display database tables.
     * @param create The DSLContext to run SQL queries with
     * @param tables The tables to display
     */
    public VisualizerFrame(DSLContext create, List<Table<?>> tables) {
        super("Database Visualizer");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane pane = new JTabbedPane();
        tables.forEach(t -> pane.addTab(t.getName(), new TablePanel(create, t)));
        add(pane, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }
}
