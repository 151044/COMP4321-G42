package hk.ust.comp4321.db.visual;

import hk.ust.comp4321.db.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.List;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for selecting which table to display to the user.
 */
public class TableSelectorPanel extends JPanel {
    private final GridBagConstraints cons = new GridBagConstraints();
    private static final List<String> TYPES = List.of("body", "title");
    private static final List<String> EXCLUDED = List.of("Document", "DocumentLink", "WordIndex", "ForwardIndex");
    private TablePanel tablePanel = null;
    private final JComboBox<String> stemName;
    private final JComboBox<String> tableType = new JComboBox<>(TYPES.toArray(new String[]{}));

    private static final List<Class<?>> TABLE_TYPES = List.of(Integer.class, Integer.class, Integer.class, Integer.class, String.class);
    private static final List<String> COLUMN_NAMES = List.of("docId", "paragraph", "sentence", "location", "rawWord");

    /**
     * Constructs a new TableSelectorPanel.
     * @param create The DSL context to send SQL queries with
     * @param tables The List of tables to filter by
     * @param conn The database connection to lookup stem IDs with
     */
    public TableSelectorPanel(DSLContext create, List<Table<?>> tables, DatabaseConnection conn) {
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.gridheight = 1;
        cons.fill = GridBagConstraints.BOTH;
        cons.gridx = 0;
        cons.gridy = 0;
        cons.weightx = 0.0;
        cons.weighty = 0.0;

        setLayout(new GridBagLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel("Stem: "));
        List<String> stemNames = tables.stream()
                .filter(t -> !EXCLUDED.contains(t.getName()))
                .map(t -> t.getName().split("_"))
                .filter(s -> s[0].equals("body"))
                .map(s -> conn.bodyOperator().getStemFromId(Integer.parseInt(s[1])))
                .distinct()
                .toList();
        stemName = new JComboBox<>(stemNames.toArray(new String[]{}));
        stemName.setEditable(true);
        panel.add(stemName);
        panel.add(new JLabel("Type: "));
        panel.add(tableType);

        tableType.addActionListener(ignored -> SwingUtilities.invokeLater(() -> {
            stemName.setEditable(false);
            stemName.removeAllItems();
            tables.stream()
                    .filter(t -> !EXCLUDED.contains(t.getName()))
                    .map(t -> t.getName().split("_"))
                    .filter(s -> s[0].equals(tableType.getSelectedItem()))
                    .map(s -> conn.bodyOperator().getStemFromId(Integer.parseInt(s[1])))
                    .distinct()
                    .forEach(stemName::addItem);
            stemName.setEditable(true);
        }));

        JButton submit = new JButton("Submit Query");
        submit.addActionListener(ignored -> {
            String stem = (String) stemName.getSelectedItem();
            String type = (String) tableType.getSelectedItem();
            int id = switch (type) {
                case "body" -> conn.bodyOperator().getIdFromStem(stem);
                case "title" -> conn.titleOperator().getIdFromStem(stem);
                default -> throw new IllegalStateException("No such type: " + type);
            };
            String tableName = type + "_" + id;
            if (tables.stream().noneMatch(t -> t.getName().equals(tableName))) {
                JOptionPane pane = new JOptionPane("The table does not exist!", JOptionPane.ERROR_MESSAGE);
                JDialog dialog = pane.createDialog(null, "Error!");
                dialog.setModalityType(Dialog.ModalityType.MODELESS);
                dialog.setVisible(true);
            } else {
                if (tablePanel != null) {
                    remove(tablePanel);
                }
                tablePanel = new TablePanel(create, tables.stream()
                        .filter(s -> s.getName().equals(tableName))
                        .findFirst().orElseThrow(), TABLE_TYPES, COLUMN_NAMES);
                add(tablePanel, cons);
                invalidate();
                revalidate();
                repaint();
            }
        });
        panel.add(submit);

        add(panel, cons);

        cons.gridy++;
        cons.gridheight = GridBagConstraints.REMAINDER;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.weightx = 1.0;
        cons.weighty = 1.0;
    }
}
