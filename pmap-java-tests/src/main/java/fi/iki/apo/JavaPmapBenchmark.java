package fi.iki.apo;

import fi.iki.apo.pmap.JavaApiMap;
import fi.iki.apo.pmap.PartitionedOpsPerThreadMap;
import fi.iki.apo.pmap.SingleOpPerThreadMap;
import fi.iki.apo.pmap.LoadGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

@Warmup(iterations = 16)
@Measurement(iterations = 16)
@BenchmarkMode(Mode.AverageTime)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
@Fork(value = 1, warmups = 0)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class JavaPmapBenchmark {

    public static final String FAST = "fast";
    public static final String SLOW = "slow";
    public static final String THOUSANDTHOUSAND = "1000*1000";
    public static final String MILLION = "1000000";
    @Param({FAST, SLOW})
    private String loadGeneratorType;
    @Param({THOUSANDTHOUSAND, MILLION})
    private String itemCount;

    private List<Integer> millionItems;
    private List<List<Integer>> thousandThousandItems;

    @Setup(Level.Invocation)
    public void setup() {
        millionItems = LoadGenerator.listOf(1000000, i -> i);
        thousandThousandItems = LoadGenerator.listOf(1000, i -> LoadGenerator.listOf(1000, n -> n));
    }

    private Function<Integer, Integer> resolveLoadGenerator() {
        switch (loadGeneratorType) {
            case FAST:
                return LoadGenerator::looperFast;
            case SLOW:
                return LoadGenerator::looperSlow;
        }
        throw new RuntimeException("Unsupported LoadGenerator type " + loadGeneratorType);
    }

    private List<Integer> runBenchmark(BiFunction<List<Integer>, Function<Integer, Integer>, List<Integer>> f) {
        final var loadGenerator = resolveLoadGenerator();
        switch (itemCount) {
            case MILLION:
                return f.apply(millionItems, loadGenerator);
            case THOUSANDTHOUSAND: {
                final var results = new ArrayList<Integer>();
                for (final var list : thousandThousandItems) {
                    results.addAll(f.apply(list, loadGenerator));
                }
                return results;
            }
        }
        throw new RuntimeException("Unsupported ItemCount " + loadGeneratorType);
    }

    @Benchmark
    public List<Integer> mapFor() {
        return runBenchmark(JavaApiMap::mapFor);
    }

    @Benchmark
    public List<Integer> mapStream() {
        return runBenchmark(JavaApiMap::mapStream);
    }

    @Benchmark
    public List<Integer> pmapParallelStream() {
        return runBenchmark(JavaApiMap::pmapParallelStream);
    }

    @Benchmark
    public List<Integer> pmapFixedThreadPool() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedThreadPool);
    }

    @Benchmark
    public List<Integer> pmapFixedThreadPoolDoubleThreads() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedThreadPoolDoubleThreads);
    }

    @Benchmark
    public List<Integer> pmapFixedVirtualThreadPool() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedVirtualThreadPool);
    }
    @Benchmark
    public List<Integer> pmapFixedReusedVirtualThreadPool() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedReusedVirtualThreadPool);
    }

    @Benchmark
    public List<Integer> pmapFixedVirtualThreadPoolDoubleThreads() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedVirtualThreadPoolDoubleThreads);
    }

    @Benchmark
    public List<Integer> pmapVirtualThread() {
        return runBenchmark(SingleOpPerThreadMap::pmapNewVirtualThread);
    }

    @Benchmark
    public List<Integer> pmapFixedThreadPoolFastCreateResolve() {
        return runBenchmark(SingleOpPerThreadMap::pmapFixedThreadPoolFastCreateResolve);
    }

    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedReusedCpu() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegmentCpu::pmapPartitionSegmentFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedCpu() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegmentCpu::pmapPartitionSegmentFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFJCpu() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegmentCpu::pmapPartitionSegmentFJ);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedReused500() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment500::pmapPartitionSegmentFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixed500() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment500::pmapPartitionSegmentFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFJ500() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment500::pmapPartitionSegmentFJ);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedReused1000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment1000::pmapPartitionSegmentFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixed1000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment1000::pmapPartitionSegmentFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFJ1000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment1000::pmapPartitionSegmentFJ);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedReused2000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment2000::pmapPartitionSegmentFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixed2000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment2000::pmapPartitionSegmentFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFJ2000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment2000::pmapPartitionSegmentFJ);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixedReused4000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment4000::pmapPartitionSegmentFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFixed4000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment4000::pmapPartitionSegmentFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionSegmentFJ4000() {
        return runBenchmark(PartitionedOpsPerThreadMap.partitionSegment4000::pmapPartitionSegmentFJ);
    }

    @Benchmark
    public List<Integer> pmapPartitionModuloFixedReused() {
        return runBenchmark(PartitionedOpsPerThreadMap::pmapPartitionModuloFixedReused);
    }
    @Benchmark
    public List<Integer> pmapPartitionModuloFixed() {
        return runBenchmark(PartitionedOpsPerThreadMap::pmapPartitionModuloFixed);
    }
    @Benchmark
    public List<Integer> pmapPartitionModuloFJ() {
        return runBenchmark(PartitionedOpsPerThreadMap::pmapPartitionModuloFJ);
    }

}
