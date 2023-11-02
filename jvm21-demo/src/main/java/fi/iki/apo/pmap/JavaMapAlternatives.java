package fi.iki.apo.pmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class JavaMapAlternatives {

    public static class JavaMapFn {
        static final ThreadPoolExecutor reusedExecutorService = new ThreadPoolExecutor(
                getCpuCount(),
                getCpuCount(),
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue(),
                Thread.ofVirtual().factory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        static {
            Runtime.getRuntime().addShutdownHook(new Thread(reusedExecutorService::shutdownNow));
        }

        public static final <T, R> List<R> mapFastest(List<T> list, Function<T, R> f) {
            return pmapParallelStream(list, f);
        }

        public static final <T, R> List<R> mapFor(List<T> list, Function<T, R> f) {
            final var dest = new ArrayList<R>(list.size());
            for (T t : list) {
                dest.add(f.apply(t));
            }
            return dest;
        }

        public static final <T, R> List<R> mapStream(List<T> list, Function<T, R> f) {
            return list.stream().map(f).toList();
        }

        public static final <T, R> List<R> pmapParallelStream(List<T> list, Function<T, R> f) {
            return list.parallelStream().map(f).toList();
        }

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
            return pmapFixedThreadPool(list, f, getCpuCount());
        }

        public static <T, R> List<R> pmapFixedThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
            return pmapFixedThreadPool(list, f, 2 * getCpuCount());
        }

        public static <T, R> List<R> pmapFixedThreadPoolFastCreateResolve(List<T> list, Function<T, R> f) {
            final List<Callable<R>> tasks = createTasks(list, f);
            try (final var executorService = Executors.newFixedThreadPool(getCpuCount())) {
                final var futures = executorService.invokeAll(tasks);
                return mapFastest(futures, JavaMapFn::resolveFuture);
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
            return pmapFixedVirtualThreadPool(list, f, getCpuCount());
        }

        private static int getCpuCount() {
            return Runtime.getRuntime().availableProcessors();
        }

        public static <T, R> List<R> pmapFixedVirtualThreadPoolDoubleThreads(List<T> list, Function<T, R> f) {
            return pmapFixedVirtualThreadPool(list, f, getCpuCount());
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
                final var futures = reusedExecutorService.invokeAll(tasks);
                return resolveFutures(futures);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static <R> List<R> resolveFutures(List<Future<R>> futures) {
            return mapFastest(futures, JavaMapFn::resolveFuture);
        }

        private static <T, R> List<Callable<R>> createTasks(List<T> list, Function<T, R> f) {
            final Function<T, Callable<R>> taskCreator = (i) -> () -> f.apply(i);
            return mapFastest(list, taskCreator);
        }

        private static <R> R resolveFuture(Future<R> fut) {
            try {
                return fut.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


}
