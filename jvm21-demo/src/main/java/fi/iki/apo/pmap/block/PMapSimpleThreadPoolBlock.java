package fi.iki.apo.pmap.block;

import fi.iki.apo.pmap.MultipleOpsPerThreadMap;
import fi.iki.apo.pmap.PMapAlternative;
import fi.iki.apo.pmap.TasksAndArray;
import fi.iki.apo.pmap.simplethreadpool.SimpleThreadPool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

public record PMapSimpleThreadPoolBlock(
        BlockRangeFactory blockRangeFactory,
        SimpleThreadPool pool
) implements PMapAlternative {
    @Override
    public <T, R> List<R> pmap(List<T> list, Function<T, R> f) {
        final var tasksWithResultArray = TasksAndArray.createBlockTasks(list.size(),
                blockRangeFactory.resolveBlockRanges(list.size()),
                (arr, min, max) -> (Callable<Boolean>) () -> {
                    BlockProcessor.mapBlock(list, arr, min, max, f);
                    return true;
                });
        return MultipleOpsPerThreadMap.executeTasksInSimplePool(pool, tasksWithResultArray);
    }
}
