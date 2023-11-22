package fi.iki.apo.pmap.block;

import fi.iki.apo.pmap.JavaMapAlternatives;
import fi.iki.apo.pmap.PMapAlternative;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static fi.iki.apo.pmap.JavaMapAlternatives.getCpuCount;

public class BlockProcessor {
    public final PMapExecutorServiceBlock reusedFixedThreadPool;
    public final PMapExecutorServiceBlock reusedFixedThreadPoolDoubleThreads;
    public final PMapExecutorServiceBlock reusedVirtualFixedThreadPool;
    public final PMapExecutorServiceBlock reusedVirtualFixedThreadPoolDoubleThreads;
    public final PMapAlternative commonFJPool;
    private BlockRangeFactory blockRangeFactory;

    public BlockProcessor(Integer blockSize, Integer blockCount) {
        blockRangeFactory = new BlockRangeFactory(blockSize, blockCount);
        reusedFixedThreadPool = new PMapExecutorServiceBlock(blockRangeFactory, JavaMapAlternatives.reusedFixedThreadPool);
        reusedFixedThreadPoolDoubleThreads = new PMapExecutorServiceBlock(blockRangeFactory, JavaMapAlternatives.reusedFixedThreadPoolDoubleThreads);
        reusedVirtualFixedThreadPool = new PMapExecutorServiceBlock(blockRangeFactory, JavaMapAlternatives.reusedVirtualFixedThreadPool);
        reusedVirtualFixedThreadPoolDoubleThreads = new PMapExecutorServiceBlock(blockRangeFactory, JavaMapAlternatives.reusedVirtualFixedThreadPoolDoubleThreads);
        commonFJPool = new PMapFJBlock(blockRangeFactory);
    }

    public static <T, R> void mapBlock(List<T> list, Object[] rArr, int lowerLimit, int upperLimit, Function<T, R> f) {
        for (int c = lowerLimit; c <= upperLimit; c++) {
            rArr[c] = f.apply(list.get(c));
        }
    }

    public <T, R> List<R> pmapBlockFixed(List<T> list, Function<T, R> f) {
        try (final var executorService = Executors.newFixedThreadPool(getCpuCount())) {
            return new PMapExecutorServiceBlock(blockRangeFactory, executorService).pmap(list, f);
        }
    }

}
