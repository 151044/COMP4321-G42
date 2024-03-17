package hk.ust.comp4321.db.visual;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model implemented with a backing list of columns.
 */
public class ListTableModel extends AbstractTableModel {
    private final List<ListTableColumn<?>> columns;

    /**
     * Constructs a new ListTableModel with the list of columns.
     * @param cols The list of columns to use
     */
    public ListTableModel(List<ListTableColumn<?>> cols) {
        columns = new ArrayList<>(cols);
    }
    @Override
    public int getRowCount() {
        return columns.get(0).size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return columns.get(col).data.get(row);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ListTableColumn col = columns.get(columnIndex); // raw types: runtime type checked by explicit check below
        checkClasses(col.getColumnClass(), aValue.getClass());
        col.set(rowIndex, aValue); // unchecked insertion, as above
    }

    /**
     * Sets the values of an entire row.
     * @param rowIndex The row index to set
     * @param objects The objects to insert into the table
     */
    public void setRow(int rowIndex, Object... objects) {
        checkBounds(objects);
        for (int i = 0; i < objects.length; i++) {
            setValueAt(objects[i], rowIndex, i);
        }
    }

    /**
     * Removes the last row from the table.
     */
    public void removeRow() {
        int len = columns.get(0).size() - 1;
        if (len < 0) {
            throw new IllegalStateException("Unable to remove from empty list model.");
        }
        columns.forEach(c -> c.remove(len));
    }

    /**
     * Appends a row to the end of this table.
     * @param objects The row of objects to append
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addRow(Object... objects) {
        checkBounds(objects);
        for (int i = 0; i < columns.size(); i++) {
            ListTableColumn col = columns.get(i); // raw types: runtime type checked by explicit check below
            checkClasses(col.getColumnClass(), objects[i].getClass());
            col.add(objects[i]); // unchecked insertion, as above
        }
    }

    @Override
    public int findColumn(String columnName) {
        return columns.stream().map(ListTableColumn::getName).toList().indexOf(columnName);
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).getName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).getColumnClass();
    }

    /**
     * Represents a column in a ListTableModel.
     * The column is backed by a List, and most of its methods delegate to their usual List counterparts.
     * @param <T> The type of objects in this column
     */
    public static class ListTableColumn<T> {
        private final List<T> data = new ArrayList<>();
        private final Class<T> clazz;
        private final String name;

        /**
         * Constructs a new ListTableColumn with the specified name and type.
         * @param name The name of the column
         * @param clazz The type of objects which can be held by the column
         */
        public ListTableColumn(String name, Class<T> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        /**
         * Appends an object to the end of this column.
         * @param t The object to append
         */
        public void add(T t) {
            data.add(t);
        }

        /**
         * Gets the number of entries in this column.
         * @return The size of this column
         */
        public int size() {
            return data.size();
        }

        /**
         * Sets the input object at the specified index
         * @param i The index to set at
         * @param t The object to set
         * @return The previous value at the index
         */
        public T set(int i, T t) {
            return data.set(i, t);
        }

        /**
         * Removes the object at that index.
         * @param i The index to remove
         * @return The removed object
         */
        public T remove(int i) {
            return data.remove(i);
        }

        /**
         * Removes the given object from this column if it exists.
         * @param t The object to remove
         * @return True if the object is successfully removed, false otherwise
         */
        public boolean remove(T t) {
            return data.remove(t);
        }

        /**
         * Gets the type of objects of the column.
         * @return The class object of the type of this column
         */
        public Class<T> getColumnClass() {
            return clazz;
        }

        /**
         * Gets the name of this column.
         * @return The name of this column
         */
        public String getName() {
            return name;
        }
    }
    private void checkBounds(Object... objects) {
        if (objects.length != columns.size()) {
            throw new IllegalArgumentException("Mismatched lengths: Expected " +
                    columns.size() + " arguments but got " + objects.length);
        }
    }
    private void checkClasses(Class<?> expected, Class<?> actual) {
        if (!expected.equals(actual)) {
            throw new ClassCastException("Class mismatch: Expected " +
                    expected + " but got " + actual);
        }
    }
}
