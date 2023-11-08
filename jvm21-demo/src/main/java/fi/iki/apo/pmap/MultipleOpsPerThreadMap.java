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

public class MultipleOpsPerThreadMap {
    public static BlockProcessor blockProcessorCpu = new BlockProcessor(null, getCpuCount());
    public static BlockProcessor blockProcessor250 = new BlockProcessor(250, null);
    public static BlockProcessor blockProcessor500 = new BlockProcessor(500, null);
    public static BlockProcessor blockProcessor1000 = new BlockProcessor(1000, null);
    public static BlockProcessor blockProcessor2000 = new BlockProcessor(2000, null);
    public static BlockProcessor blockProcessor4000 = new BlockProcessor(4000, null);
    public static BlockProcessor blockProcessor8000 = new BlockProcessor(8000, null);
    public static BlockProcessor blockProcessor16000 = new BlockProcessor(16000, null);
    public static BlockProcessor blockProcessor32000 = new BlockProcessor(32000, null);

    public record BlockProcessor(Integer blockSize, Integer blockCount) {

        private <T> List<BlockRange> resolveBlockCount(List<T> list) {
            if (blockCount != null) {
                return BlockRange.splitByBlockCount(list.size(), blockCount);
            } else if (blockSize != null) {
                return BlockRange.splitByBlockSize(list.size(), blockSize);
            }
            throw new RuntimeException("Needs blocksize or blockCount");
        }

        @NotNull
        public <T, R> List<R> pmapBlockFixedReusedVT(List<T> list, Function<T, R> f) {
            final var result = TasksAndArray.createBlockTasks(list.size(), resolveBlockCount(list), (arr, min, max) -> (Callable<Boolean>) () -> {
                mapBlock(list, arr, min, max, f);
                return true;
            });
            try {
                for (var future : JavaMapAlternatives.reusedVirtualFixedThreadPool.invokeAll(result.tasks)) {
                    future.get();
                }
                return Arrays.asList((R[]) result.rArr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @NotNull
        public <T, R> List<R> pmapBlockFixedReusedDoubleVT(List<T> list, Function<T, R> f) {
            final var result = TasksAndArray.createBlockTasks(list.size(), resolveBlockCount(list), (arr, min, max) -> (Callable<Boolean>) () -> {
                mapBlock(list, arr, min, max, f);
                return true;
            });
            try {
                for (var future : JavaMapAlternatives.reusedVirtualFixedThreadPool.invokeAll(result.tasks)) {
                    future.get();
                }
                return Arrays.asList((R[]) result.rArr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        public <T, R> List<R> pmapBlockFixedReused(List<T> list, Function<T, R> f) {
            final var result = TasksAndArray.createBlockTasks(list.size(), resolveBlockCount(list), (arr, min, max) -> (Callable<Boolean>) () -> {
                mapBlock(list, arr, min, max, f);
                return true;
            });
            try {
                for (var future : JavaMapAlternatives.reusedVirtualFixedThreadPool.invokeAll(result.tasks)) {
                    future.get();
                }
                return Arrays.asList((R[]) result.rArr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        public <T, R> List<R> pmapBlockFixed(List<T> list, Function<T, R> f) {
            final var result = TasksAndArray.createBlockTasks(list.size(), resolveBlockCount(list), (arr, min, max) -> (Callable<Boolean>) () -> {
                mapBlock(list, arr, min, max, f);
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

        @NotNull
        public <T, R> List<R> pmapBlockFJ(List<T> list, Function<T, R> f) {
            final var result = TasksAndArray.createBlockTasks(list.size(), resolveBlockCount(list), (arr, min, max) -> new ForkJoinProcessTask(null, () -> mapBlock(list, arr, min, max, f)));
            ForkJoinPool.commonPool().invoke(new ForkJoinProcessTask(result.tasks, null));
            return Arrays.asList((R[]) result.rArr);
        }
    }

    public static <T, R> List<R> pmapModuloFixedReused(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> (Callable<Boolean>) () -> {
            mapWithModulo(list, arr, startIndex, jumpSize, f);
            return true;
        });
        try {
            for (var future : JavaMapAlternatives.reusedVirtualFixedThreadPool.invokeAll(result.tasks)) {
                future.get();
            }
            return Arrays.asList((R[]) result.rArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, R> List<R> pmapModuloFixed(List<T> list, Function<T, R> f) {
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

    public static <T, R> List<R> pmapModuloFJ(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> new ForkJoinProcessTask(null, () -> mapWithModulo(list, arr, startIndex, jumpSize, f)));
        ForkJoinPool.commonPool().invoke(new ForkJoinProcessTask(result.tasks, null));
        return Arrays.asList((R[]) result.rArr);
    }

    private record TasksAndArray<M>(List<M> tasks, Object[] rArr) {
        @NotNull
        private static <M> TasksAndArray<M> createBlockTasks(int size, List<BlockRange> blockRanges, Function3<Object[], Integer, Integer, M> mapF) {
            final var rArr = new Object[size];
            final var tasks = JavaMapAlternatives.mapFastest(blockRanges, s -> mapF.invoke(rArr, s.min(), s.max()));
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

    private static <T, R> void mapBlock(List<T> list, Object[] rArr, int lowerLimit, int upperLimit, Function<T, R> f) {
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

    private static class ForkJoinProcessTask extends RecursiveAction {

        private final List<ForkJoinProcessTask> tasks;
        private final Runnable runnable;

        private ForkJoinProcessTask(List<ForkJoinProcessTask> tasks, Runnable runnable) {
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
