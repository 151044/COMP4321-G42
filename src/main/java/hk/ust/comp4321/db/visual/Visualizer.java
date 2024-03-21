package hk.ust.comp4321.db.visual;

import com.formdev.flatlaf.FlatDarculaLaf;
import hk.ust.comp4321.db.DatabaseConnection;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Main class to visualize a SQLite database.
 */
public class Visualizer {
    /**
     * The main method for the visualizer.
     * @param args The command line arguments; ignored.
     * @throws UnsupportedLookAndFeelException If FlatDarculaLaf cannot be loaded
     * @throws SQLException If an SQL exception is thrown by the database
     */
    public static void main(String[] args) throws UnsupportedLookAndFeelException, SQLException {
        UIManager.setLookAndFeel(new FlatDarculaLaf());
        JFileChooser chooser = new JFileChooser(Path.of("").toAbsolutePath().toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = chooser.showOpenDialog(null);
        File f;
        if (ret == JFileChooser.APPROVE_OPTION) {
            f = chooser.getSelectedFile();
            if (!f.exists()) {
                System.err.println("Chosen file does not exist!");
                return;
            }
        } else {
            System.err.println("Please choose a file.");
            return;
        }
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
        DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
        DatabaseConnection connection = new DatabaseConnection(f.toPath());
        List<Table<?>> tables = create.meta().getTables();
        new VisualizerFrame(create, tables, connection);
    }
}
