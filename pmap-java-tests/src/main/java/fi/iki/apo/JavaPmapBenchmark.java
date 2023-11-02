package fi.iki.apo;

import fi.iki.apo.pmap.JavaMapAlternatives.JavaMapFn;
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
    private  List<List<Integer>> thousandThousandItems;

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

    private List<Integer> runBenchmark(BiFunction<List<Integer>, Function<Integer,Integer>, List<Integer>> f) {
        final var loadGenerator = resolveLoadGenerator();
        switch (itemCount) {
            case MILLION: return f.apply(millionItems, loadGenerator);
            case THOUSANDTHOUSAND: {
                final var results = new ArrayList<Integer>();
                for(final var list : thousandThousandItems) {
                    results.addAll(f.apply(list, loadGenerator));
                }
                return results;
            }
        }
        throw new RuntimeException("Unsupported ItemCount " + loadGeneratorType);
    }

    @Benchmark
    public List<Integer> mapFor() {
        return runBenchmark(JavaMapFn::mapFor);
    }
    @Benchmark
    public List<Integer> mapStream() {
        return runBenchmark(JavaMapFn::mapStream);
    }
    @Benchmark
    public List<Integer> pmapParallelStream() {
        return runBenchmark(JavaMapFn::pmapParallelStream);
    }
    @Benchmark
    public List<Integer> pmapFixedThreadPool() {
        return runBenchmark(JavaMapFn::pmapFixedThreadPool);
    }
    @Benchmark
    public List<Integer> pmapFixedThreadPoolDoubleThreads() {
        return runBenchmark(JavaMapFn::pmapFixedThreadPoolDoubleThreads);
    }
    @Benchmark
    public List<Integer> pmapFixedVirtualThreadPool() {
        return runBenchmark(JavaMapFn::pmapFixedVirtualThreadPool);
    }

    @Benchmark
    public List<Integer> pmapFixedVirtualThreadPoolDoubleThreads() {
        return runBenchmark(JavaMapFn::pmapFixedVirtualThreadPoolDoubleThreads);
    }
    @Benchmark
    public List<Integer> pmapVirtualThread() {
        return runBenchmark(JavaMapFn::pmapNewVirtualThread);
    }

    @Benchmark
    public List<Integer> pmapFixedThreadPoolFastCreateResolve() {
        return runBenchmark(JavaMapFn::pmapFixedThreadPoolFastCreateResolve);
    }

    @Benchmark
    public List<Integer> pmapFixedReusedVirtualThreadPool() {
        return runBenchmark(JavaMapFn::pmapFixedReusedVirtualThreadPool);
    }
}
