package hk.ust.comp4321.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection implements AutoCloseable {
    private Connection conn;
    public DatabaseConnection(Path path) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());
    }

    @Override
    public void close() throws Exception {
         conn.close();
    }
}
