package fi.iki.apo.pmap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class JavaMapAlternatives {

    public static class JavaMapFn {
        public static final <T,R> List<R> mapFor(List<T> list, Function<T, R> f) {
            final var dest = new ArrayList<R>(list.size());
            for(T t : list) {
                dest.add(f.apply(t));
            }
            return dest;
        }

        public static final <T,R> List<R> mapStream(List<T> list, Function<T, R> f) {
            final var dest = new ArrayList<R>(list.size());
            for(T t : list) {
                dest.add(f.apply(t));
            }
            return list.stream().map(f).toList();
        }
        public static final <T,R> List<R> pmapParallelStream(List<T> list, Function<T, R> f) {
            final var dest = new ArrayList<R>(list.size());
            for(T t : list) {
                dest.add(f.apply(t));
            }
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
            final var tasks = createTasks(list, f);
            try (final var executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
                final var futures = executorService.invokeAll(tasks);
                return resolveFutures(futures);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static <T, R> List<R> pmapFixedVirtualThreadPool(List<T> list, Function<T, R> f) {
            final var tasks = createTasks(list, f);
            final var cpus = Runtime.getRuntime().availableProcessors();
            try (final var executorService =new ThreadPoolExecutor(
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



        private static <R> List<R> resolveFutures(List<Future<R>> futures) {
            return futures.stream().map(JavaMapFn::resolveFuture).toList();
        }

        private static <T, R> List<Callable<R>> createTasks(List<T> list, Function<T, R> f) {
            final Function<T, Callable<R>> taskCreator =  (i) -> () -> f.apply(i);
            final var tasks = list.stream().map(taskCreator).toList();
            return tasks;
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
