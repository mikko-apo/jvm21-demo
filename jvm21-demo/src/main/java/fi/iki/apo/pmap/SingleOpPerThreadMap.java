package fi.iki.apo.pmap;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class SingleOpPerThreadMap {

    public static <T, R> List<R> pmapNewVirtualThread(List<T> list, Function<T, R> f) {
        return mapItemsWithExecutorAndClose(list, f, Executors.newVirtualThreadPerTaskExecutor());
    }

    public static <T, R> List<R> pmapFixedReusedVirtualThreadPool(List<T> list, Function<T, R> f) {
        return mapItemsWithExecutor(list, f, JavaMapAlternatives.reusedVirtualFixedThreadPool);
    }

    public static <T, R> List<R> pmapFixedThreadPool(List<T> list, Function<T, R> f) {
        int nThreads = JavaMapAlternatives.getCpuCount();
        return mapItemsWithExecutorAndClose(list, f, Executors.newFixedThreadPool(nThreads));
    }

    public static <T, R> List<R> pmapFixedThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
        int nThreads = 2 * JavaMapAlternatives.getCpuCount();
        return mapItemsWithExecutorAndClose(list, f, Executors.newFixedThreadPool(nThreads));
    }

    public static <T, R> List<R> pmapFixedVirtualThreadPool(List<T> list, Function<T, R> f) {
        int nThreads = JavaMapAlternatives.getCpuCount();
        return mapItemsWithExecutorAndClose(list, f, createFixedVirtualThreadPool(nThreads));
    }

    public static <T, R> List<R> pmapFixedVirtualThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
        int nThreads = 2 * JavaMapAlternatives.getCpuCount();
        return mapItemsWithExecutorAndClose(list, f, createFixedVirtualThreadPool(nThreads));
    }

    private static <T, R> List<R> mapItemsWithExecutorAndClose(List<T> list, Function<T, R> f, ExecutorService es) {
        try (final var executorService = es) {
            return mapItemsWithExecutor(list, f, executorService);
        }
    }

    private static ExecutorService createFixedVirtualThreadPool(int nThreads) {
        return new ThreadPoolExecutor(
                nThreads,
                nThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(),
                Thread.ofVirtual().factory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    private static <T, R> List<R> mapItemsWithExecutor(List<T> list, Function<T, R> f, ExecutorService virtualThreadExecutor) {
        final Function<T, Callable<R>> createMapItemTask = (i) -> () -> f.apply(i);
        try {
            final var tasks = JavaMapAlternatives.mapFastest(list, createMapItemTask);
            final var futures = virtualThreadExecutor.invokeAll(tasks);
            return resolveFutures(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <R> List<R> resolveFutures(List<Future<R>> futures) {
        return JavaMapAlternatives.mapFastest(futures, fut -> {
            try {
                return fut.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
