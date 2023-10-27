package fi.iki.apo.pmap;

import fi.iki.apo.util.PerfTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static fi.iki.apo.pmap.JavaMapAlternatives.JavaMapFn;

public class ParallelMapPerfTest {
    long results = 0;
    private int testItemCount = 1000000;

    @Test
    public void perfFastLooper() {
        results += executePerformanceTests(16, testItemCount, this::listOfInts, LoadGenerator::looperFast);
    }

    @Test
    public void perfSlowLooper() {
        results += executePerformanceTests(8, testItemCount, this::listOfInts, LoadGenerator::looperSlow);
    }

    @Test
    public void perfMathPowSqrtFast() {
        results += executePerformanceTests(8, testItemCount/10, this::listOfInts, LoadGenerator::powSqrt);
    }
    @Test
    public void perfMathPowSqrtSlow() {
        results += executePerformanceTests(8, testItemCount, this::listOfInts, LoadGenerator::powSqrt);
    }

    private List<Integer> listOfInts(int i) {
        final var arr = new ArrayList<Integer>(i);
        for (int c = 0; c < i; c++) {
            arr.add(c);
        }
        return arr;
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

        perf.addTestRun("map with Java for(T t : list)", (l) -> JavaMapFn.mapFor(l, testF).size())
                .addTestRun("map with Java list.stream()", (l) -> JavaMapFn.mapStream(l, testF).size())
                .addTestRun("pmap with Java list.parallelStream()", (l) -> JavaMapFn.pmapParallelStream(l, testF).size())
                .addTestRun("pmap with Java fixedVirtualThreads", (l) -> JavaMapFn.pmapFixedVirtualThreadPool(l, testF).size())
                .addTestRun("pmap with Java newFixedThreadPool", (l) -> JavaMapFn.pmapFixedThreadPool(l, testF).size())
                .addTestRun("pmap with Java newVirtualThreadPerTaskExecutor", (l) -> JavaMapFn.pmapNewVirtualThread(l, testF).size());

        perf.runTests(repeats, 1000);
        return perf.testRunCount();
    }
}
