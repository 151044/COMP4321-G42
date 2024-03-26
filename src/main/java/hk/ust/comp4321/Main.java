package hk.ust.comp4321;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.api.WordInfo;
import hk.ust.comp4321.db.DatabaseConnection;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The main class.
 */
public class Main {
    /**
     * The main method.
     * @param args Command-line arguments; currently unused.
     */
    public static void main(String[] args) throws SQLException, IOException {

    }

    public static void writeToFile(Path dbPath, Path outputPath) throws SQLException, IOException {
        try (DatabaseConnection conn = new DatabaseConnection(dbPath)) {
            List<Document> docs = conn.getDocuments();
            StringBuilder sb = new StringBuilder();
            docs.stream().limit(30).forEach(d -> {
                try {
                    d.retrieveFromDatabase(conn);
                    System.out.println(d.bodyFrequencies());
                    sb.append(d.titleFrequencies().entrySet().stream()
                            .sorted(Comparator.comparing(e -> e.getValue().wordLocation()))
                            .map(e -> e.getValue().rawWord())
                            .collect(Collectors.joining(" "))).append("\n");
                    sb.append(d.url().toString()).append("\n");
                    sb.append(d.lastModified()).append(", ").append(d.size()).append("\n");
                    Map<String, Long> frequencies = d.bodyFrequencies().values().stream().map(WordInfo::rawWord)
                            .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
                    sb.append(frequencies.entrySet().stream().sorted(
                            Map.Entry.<String, Long>comparingByValue().reversed())
                            .limit(10).map(e -> e.getKey() + " " + e.getValue()).collect(Collectors.joining("; ")))
                            .append("\n");
                    sb.append(d.children().stream().limit(10).map(URL::toString)
                            .collect(Collectors.joining("\n")));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                sb.append("\n-------------------------------------------------\n");
            });
            Files.write(outputPath, List.of(sb.toString()));
        }
    }
}
