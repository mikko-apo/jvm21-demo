package fi.iki.apo.examples;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class StructuredTaskScopeExample {
    public class ItemData {
    }

    enum ItemUpdateResult {
        OK
    }

    void scopeExample(List<ItemData> items) {
        try (final var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> { // launcher scope
                items.forEach(item -> {
                    // Check for early error before launching all items. Not sure if this works.
                    if (!Thread.currentThread().isInterrupted()) {
                        // launch virtual thread for each item
                        scope.fork(() -> updateItem(item));
                    }
                });
                return "";
            });
            try {
                scope.join(); // wait until all forked virtual threads are finished or one of them fails
            } catch (InterruptedException e) {
                throw new RuntimeException("scope.join() was interrupted", e);
            }
            try {
                scope.throwIfFailed();
            } catch (ExecutionException e) {
                throw new RuntimeException("subtask failed", e.getCause());
            }
        } finally {
            printItemInfo(items); // scope is closed before finally block, it's safe to print updated items
        }
    }

    private void printItemInfo(List<ItemData> items) {

    }

    private ItemUpdateResult updateItem(ItemData item) {
        return ItemUpdateResult.OK;
    }

}
