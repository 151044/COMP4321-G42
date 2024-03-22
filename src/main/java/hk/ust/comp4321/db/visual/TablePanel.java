package hk.ust.comp4321.db.visual;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel for displaying the rows and columns of a single word table in the database.
 */
public class TablePanel extends JPanel {
    private static final List<Class<?>> TYPES = List.of(Integer.class, Integer.class, Integer.class, Integer.class, String.class);
    private static final List<String> NAMES = List.of("docId", "paragraph", "sentence", "location", "rawWord");
    /**
     * Constructs a new TablePanel with the specified table.
     * @param create The context to run SQL queries with
     * @param t The table to read and display
     */
    public TablePanel(DSLContext create, Table<?> t) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        List<ListTableModel.ListTableColumn<?>> columns = new ArrayList<>();
        List<Class<?>> types = new ArrayList<>();
        for (int i = 0; i < TYPES.size(); i++) {
            Class<?> type = TYPES.get(i);
            ListTableModel.ListTableColumn<?> listTableColumn = new ListTableModel.ListTableColumn<>(NAMES.get(i), type);
            columns.add(listTableColumn);
            types.add(type);
        }
        ListTableModel model = new ListTableModel(columns);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        Result<Record> records = create.select().from(t).fetch();
        for (Record r : records) {
            List<Object> row = new ArrayList<>();
            for (int i = 0; i < r.size(); i++) {
                row.add(r.get(i, types.get(i)));
            }
            model.addRow(row.toArray());
        }
        table.setRowSorter(new TableRowSorter<>(model));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }
}
