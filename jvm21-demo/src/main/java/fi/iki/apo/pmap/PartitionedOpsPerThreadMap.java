package fi.iki.apo.pmap;

import kotlin.jvm.functions.Function3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;

import static fi.iki.apo.pmap.JavaMapAlternatives.getCpuCount;

public class PartitionedOpsPerThreadMap {
    public static <T, R> List<R> pmapPartitionSegmentFixedReused(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createSegmentTasks(list.size(), (arr, min, max) -> (Callable<Boolean>) () -> {
            mapSegment(list, arr, min, max, f);
            return true;
        });
        try {
            for (var future : JavaMapAlternatives.reusedExecutorService.invokeAll(result.tasks)) {
                future.get();
            }
            return Arrays.asList((R[]) result.rArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static <T, R> List<R> pmapPartitionSegmentFixed(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createSegmentTasks(list.size(), (arr, min, max) -> (Callable<Boolean>) () -> {
            mapSegment(list, arr, min, max, f);
            return true;
        });
        try (final var executorService = Executors.newFixedThreadPool(getCpuCount())) {
            for (var future : executorService.invokeAll(result.tasks)) {
                future.get();
            }
            return Arrays.asList((R[]) result.rArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapPartitionSegmentFJ(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createSegmentTasks(list.size(), (arr, min, max) -> new ForkJoinMapArraySegment(null, () -> mapSegment(list, arr, min, max, f)));
        ForkJoinPool.commonPool().invoke(new ForkJoinMapArraySegment(result.tasks, null));
        return Arrays.asList((R[]) result.rArr);
    }

    public static <T, R> List<R> pmapPartitionModuloFixedReused(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> (Callable<Boolean>) () -> {
            mapWithModulo(list, arr, startIndex, jumpSize, f);
            return true;
        });
        try {
            for (var future : JavaMapAlternatives.reusedExecutorService.invokeAll(result.tasks)) {
                future.get();
            }
            return Arrays.asList((R[]) result.rArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapPartitionModuloFixed(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> (Callable<Boolean>) () -> {
            mapWithModulo(list, arr, startIndex, jumpSize, f);
            return true;
        });
        try (final var executorService = Executors.newFixedThreadPool(getCpuCount())) {
            for (var future : executorService.invokeAll(result.tasks)) {
                future.get();
            }
            return Arrays.asList((R[]) result.rArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapPartitionModuloFJ(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> new ForkJoinMapArraySegment(null, () -> mapWithModulo(list, arr, startIndex, jumpSize, f)));
        ForkJoinPool.commonPool().invoke(new ForkJoinMapArraySegment(result.tasks, null));
        return Arrays.asList((R[]) result.rArr);
    }

    private record TasksAndArray<M>(List<M> tasks, Object[] rArr) {
        @NotNull
        private static <M> TasksAndArray<M> createSegmentTasks(int size, Function3<Object[], Integer, Integer, M> mapSegment) {
            final var rArr = new Object[size];
            final var segments = RangeSegment.splitRange(size, getCpuCount());
            final var tasks = JavaMapAlternatives.mapFastest(segments, s -> mapSegment.invoke(rArr, s.min, s.max));
            return new TasksAndArray<>(tasks, rArr);
        }

        @NotNull
        private static <M> TasksAndArray<M> createModuloTasks(int size, Function3<Object[], Integer, Integer, M> mapUsingModulo) {
            final var rArr = new Object[size];
            int cpuCount = getCpuCount();
            final var tasks = new ArrayList<M>(cpuCount);
            for (int c = 0; c < cpuCount; c++) {
                tasks.add(mapUsingModulo.invoke(rArr, c, cpuCount));
            }
            return new TasksAndArray<>(tasks, rArr);
        }
    }

    record RangeSegment(int min, int max) {
        static public List<RangeSegment> splitRange(int size, int handlerCount) {
            final var tasks = new ArrayList<RangeSegment>(handlerCount);
            final var minSegmentSize = size / handlerCount;
            var largerTasks = size % handlerCount;
            var lowerLimit = 0;
            while (lowerLimit < size) {
                int segmentSize = minSegmentSize;
                if (largerTasks > 0) {
                    largerTasks--;
                    segmentSize++;
                }
                int upperLimit = lowerLimit + segmentSize - 1;
                tasks.add(new RangeSegment(lowerLimit, upperLimit));
                lowerLimit += segmentSize;
            }
            return tasks;
        }
    }


    private static <T, R> void mapSegment(List<T> list, Object[] rArr, int lowerLimit, int upperLimit, Function<T, R> f) {
        for (int c = lowerLimit; c <= upperLimit; c++) {
            rArr[c] = f.apply(list.get(c));
        }
    }

    private static <T, R> void mapWithModulo(List<T> list, Object[] rArr, int startIndex, int jump, Function<T, R> f) {
        int size = list.size();
        int index = startIndex;
        while (index < size) {
            rArr[index] = f.apply(list.get(index));
            index += jump;
        }
    }

    private static class ForkJoinMapArraySegment extends RecursiveAction {

        private final List<ForkJoinMapArraySegment> tasks;
        private final Runnable runnable;

        private ForkJoinMapArraySegment(List<ForkJoinMapArraySegment> tasks, Runnable runnable) {
            this.tasks = tasks;
            this.runnable = runnable;
        }

        @Override
        protected void compute() {
            if (tasks != null) {
                invokeAll(tasks);
            } else {
                runnable.run();
            }
        }
    }
}
