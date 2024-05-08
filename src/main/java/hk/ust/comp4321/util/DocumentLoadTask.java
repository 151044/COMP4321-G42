package hk.ust.comp4321.util;

import hk.ust.comp4321.api.Document;

import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

public class DocumentLoadTask extends RecursiveAction {
    private static final int THRESHOLD = 40;
    private final List<Document> docs;
    private final Consumer<Document> cons;

    public DocumentLoadTask(List<Document> docs, Consumer<Document> cons) {
        this.docs = docs;
        this.cons = cons;
    }

    @Override
    protected void compute() {
        int size = docs.size();
        if (size <= THRESHOLD) {
            docs.forEach(cons);
        } else {
            int midpoint = size / 2;
            DocumentLoadTask task1 = new DocumentLoadTask(docs.subList(0, midpoint), cons);
            DocumentLoadTask task2 = new DocumentLoadTask(docs.subList(midpoint + 1, size), cons);
            task1.fork();
            task2.fork();
            task1.join();
            task2.join();
        }
    }
}
