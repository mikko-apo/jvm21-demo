package fi.iki.apo.pmap.block;

import fi.iki.apo.pmap.MultipleOpsPerThreadMap;
import fi.iki.apo.pmap.PMapAlternative;
import fi.iki.apo.pmap.TasksAndArray;
import fi.iki.apo.pmap.forkjoinpool.ForkJoinProcessTask;

import java.util.List;
import java.util.function.Function;

public record PMapFJBlock(BlockRangeFactory blockRangeFactory) implements PMapAlternative {
    @Override
    public <T, R> List<R> pmap(List<T> list, Function<T, R> f) {
        int size = list.size();
        final var result = TasksAndArray.createBlockTasks(size,
                blockRangeFactory.resolveBlockRanges(size),
                (arr, min, max) -> new ForkJoinProcessTask(null, () -> BlockProcessor.mapBlock(list, arr, min, max, f)));
        return MultipleOpsPerThreadMap.executeTasksInFJP(result);
    }
}
