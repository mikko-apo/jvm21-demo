package fi.iki.apo;

import fi.iki.apo.pmap.JavaMapAlternatives.JavaMapFn;
import fi.iki.apo.pmap.LoadGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
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
    @Param({FAST, SLOW})
    private String loadGeneratorType;

    private List<Integer> items;

    @Setup(Level.Invocation)
    public void setup() {
        items = LoadGenerator.listOfInts(1000000);
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

    @Benchmark
    public List<Integer> testJavaMapFor() {
        return JavaMapFn.mapFor(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaMapStream() {
        return JavaMapFn.mapStream(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapParallelStream() {
        return JavaMapFn.pmapParallelStream(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPool() {
        return JavaMapFn.pmapFixedThreadPool(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPoolDoubleThreads() {
        return JavaMapFn.pmapFixedThreadPoolDoubleThreads(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedVirtualThreadPool() {
        return JavaMapFn.pmapFixedVirtualThreadPool(items, resolveLoadGenerator());
    }

    @Benchmark
    public List<Integer> testJavaPmapFixedVirtualThreadPoolDoubleThreads() {
        return JavaMapFn.pmapFixedVirtualThreadPoolDoubleThreads(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapVirtualThread() {
        return JavaMapFn.pmapNewVirtualThread(items, resolveLoadGenerator());
    }

    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPoolFastCreateResolve() {
        return JavaMapFn.pmapFixedThreadPoolFastCreateResolve(items, resolveLoadGenerator());
    }
}
