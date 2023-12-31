package fi.iki.apo;

import fi.iki.apo.pmap.JavaApiMap;
import fi.iki.apo.pmap.MultipleOpsPerThreadMap;
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
    public List<Integer> pmapBlockFixedReusedVTCpu() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockCountCpu.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedCpu() {
        return runBenchmark(MultipleOpsPerThreadMap.blockCountCpu.reusedFixedThreadPool::pmap);
    }

    @Benchmark
    public List<Integer> pmapBlockFixedCpu() {
        return runBenchmark(MultipleOpsPerThreadMap.blockCountCpu::pmapBlockFixed);
    }

    @Benchmark
    public List<Integer> pmapBlockFJCpu() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockCountCpu.commonFJPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT500() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize500.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReused500() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize500.reusedFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixed500() {
        return runBenchmark(MultipleOpsPerThreadMap.blockSize500::pmapBlockFixed);
    }

    @Benchmark
    public List<Integer> pmapBlockFJ500() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize500.commonFJPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT1000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize1000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReused1000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize1000.reusedFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixed1000() {
        return runBenchmark(MultipleOpsPerThreadMap.blockSize1000::pmapBlockFixed);
    }

    @Benchmark
    public List<Integer> pmapBlockFJ1000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize1000.commonFJPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT2000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize2000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedDoubleVT2000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize2000.reusedVirtualFixedThreadPoolDoubleThreads.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedDouble2000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize2000.reusedFixedThreadPoolDoubleThreads.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReused2000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize2000.reusedFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixed2000() {
        return runBenchmark(MultipleOpsPerThreadMap.blockSize2000::pmapBlockFixed);
    }

    @Benchmark
    public List<Integer> pmapBlockFJ2000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize2000.commonFJPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT4000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize4000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT8000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize8000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT16000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize16000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReusedVT32000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize32000.reusedVirtualFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixedReused4000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize4000.reusedFixedThreadPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapBlockFixed4000() {
        return runBenchmark(MultipleOpsPerThreadMap.blockSize4000::pmapBlockFixed);
    }

    @Benchmark
    public List<Integer> pmapBlockFJ4000() {
        return runBenchmark((list, f) -> MultipleOpsPerThreadMap.blockSize4000.commonFJPool.pmap(list, f));
    }

    @Benchmark
    public List<Integer> pmapPartitionModuloFixedReused() {
        return runBenchmark(MultipleOpsPerThreadMap.modulo::pmapModuloFixedReused);
    }

    @Benchmark
    public List<Integer> pmapPartitionModuloFixed() {
        return runBenchmark(MultipleOpsPerThreadMap.modulo::pmapModuloFixed);
    }

    @Benchmark
    public List<Integer> pmapPartitionModuloFJ() {
        return runBenchmark(MultipleOpsPerThreadMap.modulo::pmapModuloFJ);
    }
}
