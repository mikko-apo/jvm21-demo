package fi.iki.apo.pmap.block;

import fi.iki.apo.pmap.MultipleOpsPerThreadMap;
import fi.iki.apo.pmap.PMapAlternative;
import fi.iki.apo.pmap.TasksAndArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public record PMapExecutorServiceBlock(
        BlockRangeFactory blockRangeFactory,
        ExecutorService executorService
) implements PMapAlternative {
    @Override
    public <T, R> List<R> pmap(List<T> list, Function<T, R> f) {
        final var tasksWithResultArray = TasksAndArray.createBlockTasks(list.size(),
                blockRangeFactory.resolveBlockRanges(list.size()),
                (arr, min, max) -> (Callable<Boolean>) () -> {
                    BlockProcessor.mapBlock(list, arr, min, max, f);
                    return true;
                });
        return MultipleOpsPerThreadMap.executeTasks(executorService, tasksWithResultArray);
    }

    public <T, R, Z> Z mapReduce(List<T> list, Function<T, R> map, Function<List<List<R>>, Z> reduce) {
        return reduce.apply(pmapToBlocks(list, map));
    }

    public <T, R> List<List<R>> pmapToBlocks(List<T> list, Function<T, R> map) {
        final var blockRanges = blockRangeFactory.resolveBlockRanges(list.size());
        final List<List<R>> mappedResults = pmap(blockRanges, blockRange -> {
            ArrayList<R> ret = new ArrayList<>(blockRange.size());
            int upperLimit = blockRange.max();
            for (int c = blockRange.min(); c <= upperLimit; c++) {
                ret.add(map.apply(list.get(c)));
            }
            return ret;
        });
        return mappedResults;
    }

    record Pair<K, V>(K a, V b) {
    }

    public <T> void forEach(List<T> list, Consumer<T> consumer) {
        final var blockRanges = blockRangeFactory.resolveBlockRanges(list.size());
        final var tasks = pmap(blockRanges, blockRange -> (Callable<Boolean>)() -> {
            final var upperLimit = blockRange.max();
            for (int c = blockRange.min(); c <= upperLimit; c++) {
                consumer.accept(list.get(c));
            }
            return true;
        });
        MultipleOpsPerThreadMap.executeTasks(executorService, tasks);
    }

    public <T> List<T> filter(List<T> list, Function<T, Boolean> f) {
        final var blockRanges = blockRangeFactory.resolveBlockRanges(list.size());
        final List<T[]> mappedResults = pmap(blockRanges, blockRange -> {
            ArrayList<T> ret = new ArrayList<>();
            int upperLimit = blockRange.max();
            for (int c = blockRange.min(); c <= upperLimit; c++) {
                T t = list.get(c);
                if (f.apply(t)) {
                    ret.add(t);
                }
            }
            return (T[])ret.toArray();
        });
        final var destBlockRanges = new ArrayList<Pair<T[], BlockRange>>(mappedResults.size());
        int lowerLimit = 0;
        int fullSize = 0;
        for (var arr : mappedResults) {
            int size = arr.length;
            if (size > 0) {
                destBlockRanges.add(new Pair<>(arr, new BlockRange(lowerLimit, lowerLimit + size-1)));
                fullSize += size;
                lowerLimit = fullSize;
            }
        }
        final var rArr = (T[])new Object[fullSize];
        forEach(destBlockRanges, pair -> {
            System.arraycopy(pair.a, 0, rArr, pair.b.min(), pair.b.size());
        });
        return Arrays.asList(rArr);
    }

}
