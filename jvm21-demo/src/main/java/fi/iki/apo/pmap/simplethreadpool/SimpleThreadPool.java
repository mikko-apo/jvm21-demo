package fi.iki.apo.pmap.simplethreadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimpleThreadPool {
    private final WorkerThread[] threads;
    private final BlockingQueue<Runnable> taskQueue;

    public SimpleThreadPool(int poolSize) {
        taskQueue = new LinkedBlockingQueue<>();
        threads = new WorkerThread[poolSize];
        for (int i = 0; i < poolSize; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    public <V> SimpleFuture<V> submitTask(Callable<V> task) {
        try {
            final var workerTask = new SimpleFuture<>(task);
            taskQueue.put(workerTask);
            return workerTask;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public <V> List<SimpleFuture<V>> submitCallables(List<Callable<V>> tasks) throws ExecutionException, InterruptedException {
        List<SimpleFuture<V>> futures = new ArrayList<>(tasks.size());
        for (final var task : tasks) {
            futures.add(submitTask(task));
        }
        for (final var fut : futures) {
            fut.get();
        }
        return futures;
    }
    private class WorkerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
