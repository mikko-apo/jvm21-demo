package fi.iki.apo.pmap;

import fi.iki.apo.util.CollectionHelpers;
import fi.iki.apo.util.PerfTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParallelMapPerfTest {
    long results = 0;
    private int testItemCount = 1000000;

    @Test
    public void perfFastLooper() {
        results += executePerformanceTests(16, testItemCount, CollectionHelpers::listOfInts, LoadGenerator::looperFast);
    }

    @Test
    public void perfSlowLooper() {
        results += executePerformanceTests(8, testItemCount, CollectionHelpers::listOfInts, LoadGenerator::looperSlow);
    }


    private ArrayList<Long> listOfLongs(int i) {
        final var arr = new ArrayList<Long>(i);
        for (long c = 0; c < i; c++) {
            arr.add(c);
        }
        return arr;
    }

    private static <T, R> long executePerformanceTests(int repeats, int testItemCount, Function<Integer, List<T>> testDataBuilder, Function<T, R> testF) {
        final var perf = new PerfTest<>(testDataBuilder, testItemCount, testItemCount / 10);

        perf.addWarmup("testF", list -> {
            final var warmUpResults = new ArrayList<R>(list.size());
            for (T item : list) {
                warmUpResults.add(testF.apply(item));
            }
            return warmUpResults.size();
        });
/*
        perf
                .addTestRun("map with Java for(T t : list)", (l) -> JavaApiMap.mapFor(l, testF).size())
                .addTestRun("map with Java list.stream()", (l) -> JavaApiMap.mapStream(l, testF).size())
                .addTestRun("pmap with Java list.parallelStream()", (l) -> JavaApiMap.pmapParallelStream(l, testF).size())

                .addTestRun("pmap with Java fixedVirtualThreads", (l) -> SingleOpPerThreadMap.pmapFixedVirtualThreadPool(l, testF).size())
                .addTestRun("pmap with Java newFixedThreadPool", (l) -> SingleOpPerThreadMap.pmapFixedThreadPool(l, testF).size())
                .addTestRun("pmap with Java newVirtualThreadPerTaskExecutor", (l) -> SingleOpPerThreadMap.pmapNewVirtualThread(l, testF).size())
                .addTestRun("pmap with Java fixedReusedVirtualThreadPool", (l) -> SingleOpPerThreadMap.pmapFixedReusedVirtualThreadPool(l, testF).size())
                .addTestRun("pmap with Java pmapFixedThreadPoolFastCreateResolve", (l) -> SingleOpPerThreadMap.pmapFixedThreadPoolFastCreateResolve(l, testF).size())
                .addTestRun("pmap with Java pmapFixedThreadPoolDoubleThreads", (l) -> SingleOpPerThreadMap.pmapFixedThreadPoolDoubleThreads(l, testF).size())
                .addTestRun("pmap with Java pmapFixedVirtualThreadPoolDoubleThreads", (l) -> SingleOpPerThreadMap.pmapFixedVirtualThreadPoolDoubleThreads(l, testF).size())


                .addTestRun("pmap with Java pmapSegmentBatching", (l) -> BatchOpsPerThreadMap.pmapSegmentBatching(l, testF).size())
                .addTestRun("pmap with Java pmapModuloBatching", (l) -> BatchOpsPerThreadMap.pmapModuloBatching(l, testF).size())
                .addTestRun("pmap with Java pmapForkJoinSegment", (l) -> BatchOpsPerThreadMap.pmapForkJoinSegment(l, testF).size())
                .addTestRun("pmap with Java pmapForkJoinModulo", (l) -> BatchOpsPerThreadMap.pmapForkJoinModulo(l, testF).size());

        perf
                .addTestRun("pmap with Java list.parallelStream()", (l) -> JavaApiMap.pmapParallelStream(l, testF).size())
                .addTestRun("pmap with Java pmapModuloBatching", (l) -> MultipleOpsPerThreadMap.modulo.pmapModuloFixedReused(l, testF).size())
                .addTestRun("pmap with Java pmapForkJoinModulo", (l) -> MultipleOpsPerThreadMap.modulo.pmapModuloFJ(l, testF).size())
                .addTestRun("pmap with Java pmapModuloBatchingFixed", (l) -> MultipleOpsPerThreadMap.modulo.pmapModuloFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReusedCpu", (l) -> MultipleOpsPerThreadMap.blockCountCpu.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJCPU", (l) -> MultipleOpsPerThreadMap.blockCountCpu.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedCpu", (l) -> MultipleOpsPerThreadMap.blockCountCpu.pmapBlockFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReused1000", (l) -> MultipleOpsPerThreadMap.blockSize1000.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJ1000", (l) -> MultipleOpsPerThreadMap.blockSize1000.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixed1000", (l) -> MultipleOpsPerThreadMap.blockSize1000.pmapBlockFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReused500", (l) -> MultipleOpsPerThreadMap.blockSize500.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJ500", (l) -> MultipleOpsPerThreadMap.blockSize500.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixed500", (l) -> MultipleOpsPerThreadMap.blockSize500.pmapBlockFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReused250", (l) -> MultipleOpsPerThreadMap.blockSize250.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJ250", (l) -> MultipleOpsPerThreadMap.blockSize250.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixed250", (l) -> MultipleOpsPerThreadMap.blockSize250.pmapBlockFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReused2000", (l) -> MultipleOpsPerThreadMap.blockSize2000.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJ2000", (l) -> MultipleOpsPerThreadMap.blockSize2000.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixed2000", (l) -> MultipleOpsPerThreadMap.blockSize2000.pmapBlockFixed(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixedReused4000", (l) -> MultipleOpsPerThreadMap.blockSize4000.reusedVirtualFixedThreadPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFJ4000", (l) -> MultipleOpsPerThreadMap.blockSize4000.commonFJPool.pmap(l, testF).size())
                .addTestRun("pmap with Java pmapPartitionSegmentFixed4000", (l) -> MultipleOpsPerThreadMap.blockSize4000.pmapBlockFixed(l, testF).size());
*/
        perf
                .addTestRun("pmap with Java list.parallelStream()", (l) -> JavaApiMap.pmapParallelStream(l, testF).size())
                .addTestRun("pmap with Java pmap.blockSize2000.reusedVirtualFixedThreadPool", (l) -> MultipleOpsPerThreadMap.blockSize2000.reusedVirtualFixedThreadPool.pmap(l, testF).size());

        perf.runTests(repeats, 1000);
        return perf.testRunCount();
    }

}
