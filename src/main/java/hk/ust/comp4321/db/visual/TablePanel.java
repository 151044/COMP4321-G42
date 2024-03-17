package hk.ust.comp4321.db.visual;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;

/**
 * A panel for displaying the rows and columns of a single table in the database.
 */
public class TablePanel extends JPanel {
    /**
     * Constructs a new TablePanel with the specified table.
     * @param create The context to run SQL queries with
     * @param t The table to read and display
     */
    public TablePanel(DSLContext create, Table<?> t) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        List<ListTableModel.ListTableColumn<?>> columns = new ArrayList<>();
        List<Class<?>> types = new ArrayList<>();
        for (Field<?> f : t.fields()) {
            Class<?> type = f.getType();
            ListTableModel.ListTableColumn<?> listTableColumn = new ListTableModel.ListTableColumn<>(f.getName(), type);
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
