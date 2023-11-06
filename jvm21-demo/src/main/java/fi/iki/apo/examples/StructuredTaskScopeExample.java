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
            final var launcherSubTask = scope.fork(() -> {
                items.forEach(item -> {
                    // If any subtask fails, scope interrupts all subtasks. When interrupted stop sending requests
                    if (!Thread.currentThread().isInterrupted()) {
                        // launch subtask (virtual thread) for each item
                        scope.fork(() -> updateItem(item));
                    }
                });
                return "OK";
            });
            try {
                scope.join(); // wait until all forked subtasks are finished or one of them fails
                System.out.printf("Result: " + launcherSubTask.get());
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
