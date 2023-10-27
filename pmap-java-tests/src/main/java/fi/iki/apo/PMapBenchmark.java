package fi.iki.apo;

import fi.iki.apo.pmap.JavaMapAlternatives.*;
import fi.iki.apo.pmap.LoadGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Function;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
public class PMapBenchmark {

    @Param({"1", "2"})
    private String loadGeneratorType;

    private final List<Integer> items;

    public PMapBenchmark() {
        this.items = listOfInts(1000000);
    }

    private List<Integer> listOfInts(int i) {
        final var  arr = new ArrayList<Integer>(i);
        for(int c=0;c<i;c++) {
            arr.add(c);
        }
        return arr;
    }

    private Function<Integer, Integer> resolveLoadGenerator() {
        switch (loadGeneratorType) {
            case "1": return LoadGenerator::looperFast;
            case "2": return LoadGenerator::looperSlow;
            default: throw new RuntimeException("Unsupported LoadGenerator type " + loadGeneratorType);
        }
    }

    @Benchmark
    public List<Integer> testJavaVirtualThreadPMap() {
        return JavaMapFn.pmapNewVirtualThread(items, resolveLoadGenerator());
    }

    @Benchmark
    public List<Integer> testJavaFixedThreadPoolPMap() {
        return JavaMapFn.pmapFixedThreadPool(items, resolveLoadGenerator());
    }
}
