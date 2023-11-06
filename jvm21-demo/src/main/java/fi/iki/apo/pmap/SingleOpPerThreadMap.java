package fi.iki.apo.pmap;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SingleOpPerThreadMap {

    public static <T, R> List<R> pmapNewVirtualThread(List<T> list, Function<T, R> f) {
        final var tasks = createTasks(list, f);
        try (final var virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            final var futures = virtualThreadExecutor.invokeAll(tasks);
            return resolveFutures(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapFixedThreadPool(List<T> list, Function<T, R> f) {
        return pmapFixedThreadPool(list, f, JavaMapAlternatives.getCpuCount());
    }

    public static <T, R> List<R> pmapFixedThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
        return pmapFixedThreadPool(list, f, 2 * JavaMapAlternatives.getCpuCount());
    }

    public static <T, R> List<R> pmapFixedThreadPoolFastCreateResolve(List<T> list, Function<T, R> f) {
        final List<Callable<R>> tasks = createTasks(list, f);
        try (final var executorService = Executors.newFixedThreadPool(JavaMapAlternatives.getCpuCount())) {
            final var futures = executorService.invokeAll(tasks);
            return JavaMapAlternatives.mapFastest(futures, SingleOpPerThreadMap::resolveFuture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T, R> List<R> pmapFixedThreadPool(List<T> list, Function<T, R> f, int nThreads) {
        final var tasks = createTasks(list, f);
        try (final var executorService = Executors.newFixedThreadPool(nThreads)) {
            final var futures = executorService.invokeAll(tasks);
            return resolveFutures(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T, R> List<R> pmapFixedVirtualThreadPool(List<T> list, Function<T, R> f) {
        return pmapFixedVirtualThreadPool(list, f, JavaMapAlternatives.getCpuCount());
    }

    public static <T, R> List<R> pmapFixedVirtualThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
        return pmapFixedVirtualThreadPool(list, f, JavaMapAlternatives.getCpuCount());
    }

    private static <T, R> List<R> pmapFixedVirtualThreadPool(List<T> list, Function<T, R> f, int cpus) {
        final var tasks = createTasks(list, f);
        try (final var executorService = new ThreadPoolExecutor(
                cpus,
                cpus,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(),
                Thread.ofVirtual().factory(),
                new ThreadPoolExecutor.AbortPolicy()
        )) {
            final var futures = executorService.invokeAll(tasks);
            return resolveFutures(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapFixedReusedVirtualThreadPool(List<T> list, Function<T, R> f) {
        final var tasks = createTasks(list, f);
        try {
            final var futures = JavaMapAlternatives.reusedVirtualFixedThreadPool.invokeAll(tasks);
            return resolveFutures(futures);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <R> List<R> resolveFutures(List<Future<R>> futures) {
        return JavaMapAlternatives.mapFastest(futures, SingleOpPerThreadMap::resolveFuture);
    }

    private static <T, R> List<Callable<R>> createTasks(List<T> list, Function<T, R> f) {
        final Function<T, Callable<R>> taskCreator = (i) -> () -> f.apply(i);
        return JavaMapAlternatives.mapFastest(list, taskCreator);
    }

    private static <R> R resolveFuture(Future<R> fut) {
        try {
            return fut.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
