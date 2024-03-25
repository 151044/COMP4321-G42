package hk.ust.comp4321.db.visual;

import hk.ust.comp4321.api.Document;
import hk.ust.comp4321.db.DatabaseConnection;

import javax.swing.*;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformancePanel extends JPanel {
    private JProgressBar bar;
    private JLabel timeElapsed = new JLabel();
    private JButton docButton;
    public PerformancePanel(DatabaseConnection conn) {
        int max = DatabaseConnection.nextDocId() - 1; // don't care about the consistency
        bar = new JProgressBar(0, max);
        bar.setStringPainted(true);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BoxLayout(documentPanel, BoxLayout.Y_AXIS));
        JPanel progressBarPanel = new JPanel();
        progressBarPanel.setLayout(new BoxLayout(progressBarPanel, BoxLayout.X_AXIS));
        progressBarPanel.add(new JLabel("Progress: "));
        progressBarPanel.add(bar);
        docButton = new JButton("Start!");
        docButton.addActionListener(ignored -> {
            docButton.setEnabled(false);
            new DocumentRetrieval(conn, max).execute();
        });
        progressBarPanel.add(docButton);
        documentPanel.add(progressBarPanel);
        documentPanel.add(timeElapsed);
        add(documentPanel);
    }
    private class DocumentRetrieval extends SwingWorker<Void, Void> {
        private final DatabaseConnection conn;
        private final int max;
        private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        private AtomicInteger seconds = new AtomicInteger();
        public DocumentRetrieval(DatabaseConnection conn, int max) {
            this.conn = conn;
            this.max = max;
        }
        @Override
        protected Void doInBackground() throws Exception {
            exec.scheduleAtFixedRate(() -> {
                int curSecs = seconds.getAndIncrement();
                SwingUtilities.invokeLater(() -> {
                    timeElapsed.setText("Time Elapsed: " + LocalTime.ofSecondOfDay(curSecs).toString());
                });
            }, 0, 1, TimeUnit.SECONDS);
            for (int i = 0; i < max; i++) {
                Document doc = conn.getDocFromId(i);
                doc.retrieveFromDatabase(conn);
                setProgress(100 * i / max);
                int finalI = i;
                SwingUtilities.invokeLater(() -> {
                    bar.setValue(finalI);
                    bar.setString("%s / %s (%s%%): %s".formatted(finalI, max, 100 * finalI / max, doc.url()));
                });
            }
            return null;
        }

        @Override
        protected void done() {
            exec.shutdown();
            docButton.setEnabled(true);
        }
    }
}
