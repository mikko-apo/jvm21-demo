package fi.iki.apo.pmap;

import fi.iki.apo.util.Benchmark;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fi.iki.apo.pmap.JavaMapAlternatives.JavaMapFn;
import static fi.iki.apo.util.Benchmark.benchmarkList;

public class ParallelMapPerfTest {
    long results = 0;

    @Test
    public void perfFastLooper() {
        results += performanceTest(8, listOfInts(1000000), LoadGenerator::looperFast);
    }

    @Test
    public void perfSlowLooper() {
        results += performanceTest(8, listOfInts(1000000), LoadGenerator::looperSlow);
    }

    @Test
    public void perfWithLongPrimitiveInOut() {
        results += performanceTest(8, listOfLongs(1000000), LoadGenerator::looperSlowLongParameter);
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

    static public <T, R> long performanceTest(int repeats, final List<T> items, Function<T, R> testF) {
        long results = 0;
        final var bm = new Benchmark();
        bm.print("Warming load function");
        final var warmupItems = items.stream().limit(items.size() / 10).collect(Collectors.toList());
        final var warmUpResults = new ArrayList<R>(warmupItems.size());
        for (T warmupItem : warmupItems) {
            warmUpResults.add(testF.apply(warmupItem));
        }
        results += warmUpResults.size();
        bm.print("Warming tested functions with", warmupItems.size(), "items");
        results += executePerformanceTests(repeats, warmupItems, testF, false);
        bm.print("Warming up done. Testing with", items.size(), "items");
        System.out.println("----------------------");
        results += executePerformanceTests(repeats, items, testF, true);
        System.out.println("----------------------");
        bm.print("Tests done");
        return results;
    }

    private static <T, R> long executePerformanceTests(int repeats, List<T> items, Function<T, R> testF, boolean showResult) {
        long results = 0;
        results += benchmarkList(items, repeats, showResult, "map with Java for(T t : list)", (l) -> JavaMapFn.mapFor(l, testF)).size();
        sleep1();
        results += benchmarkList(items, repeats, showResult, "map with Java list.stream()", (l) -> JavaMapFn.mapStream(l, testF)).size();
        sleep1();
        results += benchmarkList(items, repeats, showResult, "pmap with Java list.parallelStream()", (l) -> JavaMapFn.pmapParallelStream(l, testF)).size();
        sleep1();
        results += benchmarkList(items, repeats, showResult, "pmap with Java fixedVirtualThreads", (l) -> JavaMapFn.pmapFixedVirtualThreadPool(l, testF)).size();
        sleep1();
        results += benchmarkList(items, repeats, showResult, "pmap with Java newFixedThreadPool", (l) -> JavaMapFn.pmapFixedThreadPool(l, testF)).size();
        sleep1();
        results += benchmarkList(items, repeats, showResult, "pmap with Java newVirtualThreadPerTaskExecutor", (l) -> JavaMapFn.pmapNewVirtualThread(l, testF)).size();
        sleep1();
        return results;
    }

    private static void sleep1() {
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
