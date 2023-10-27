package fi.iki.apo.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static fi.iki.apo.util.StringHelpers.formatDuration;

public class PerfTest<TestData> {
    private final List<RunnableFn> runnableFns = new ArrayList<>();
    private final List<RunnableFn> warmupFns = new ArrayList<>();
    private final List<RunnableFn> testRoundFns = new ArrayList<>();
    private Integer currentTestRound;
    private int loggingIntervalSeconds = 15;

    private final Function<Integer, TestData> testDataBuilder;
    private final int testDataSize;
    private final int warmupDataSize;

    public PerfTest(Function<Integer, TestData> testDataBuilder, int testDataSize, int warmupDataSize) {
        this.testDataBuilder = testDataBuilder;
        this.testDataSize = testDataSize;
        this.warmupDataSize = warmupDataSize;
    }

    public record RunnableFn<P, R>(
            String name,
            Function<P, R> fn,
            List<RunResult<R>> runResults
    ) {
    }

    public record RunResult<D>(long duration, D result) {

    }

    public record RunResultSummary(
            String name,
            List<Long> durations,
            MinMax minMax,
            double mean,
            double percentile,
            double standardDeviation,
            double relativeStandardDeviation
    ) {

    }

    public record RunSummary(List<RunResultSummary> results, int repeats) {
        public void printResults() {
            for (RunResultSummary result : results()) {
                System.out.println(StringHelpers.joinStrings(
                        result.name,
                        repeats(),
                        "repeats:",
                        "75% percentile",
                        formatDuration((long) result.percentile),
                        "[",
                        formatDuration(result.minMax.min),
                        "-",
                        formatDuration(result.minMax.max),
                        "]",
                        "average",
                        formatDuration((long) result.mean),
                        "std dev",
                        String.format("%.2f%%", result.relativeStandardDeviation),
                        formatDuration((long) result.standardDeviation)
                ));
                System.out.println(StringHelpers.joinStrings(" - durations: ",
                        StringHelpers.joinStrings(result.durations().stream().map(StringHelpers::formatDuration).toList())
                ));
            }
        }

    }

    public <R> PerfTest<TestData> addTestRun(String name, Function<TestData, R> fn) {
        runnableFns.add(new RunnableFn<>(name, fn, new ArrayList<>()));
        return this;
    }

    public <R> PerfTest<TestData> addWarmup(String name, Function<TestData, R> fn) {
        warmupFns.add(new RunnableFn<>(name, fn, new ArrayList<>()));
        return this;
    }

    private void runRunnableFns(List<RunnableFn> fns, boolean runWarmUp, int sleepBetweenMs, boolean removeItems) {
        Object testData;
        if (runWarmUp) {
            testData = testDataBuilder.apply(warmupDataSize);
        } else {
            testData = testDataBuilder.apply(testDataSize);
        }
        if (removeItems) {
            while (!fns.isEmpty()) {
                runFn(fns.remove(0), testData, sleepBetweenMs);
            }
        } else {
            for (RunnableFn fn : fns) {
                runFn(fn, testData, sleepBetweenMs);
            }
        }
    }

    private <T,R> void runFn(RunnableFn<T,R> fn, T testData, int sleepBetweenMs) {
        Benchmark bm = new Benchmark();
        R result = fn.fn().apply(testData);
        fn.runResults.add(new RunResult<>(bm.calculateDuration(), result));
        gcAndSleep(sleepBetweenMs);
    }

    private static void clearResults(List<RunnableFn> fns) {
        for (RunnableFn fn : fns) {
            if (fn.runResults() != null) {
                fn.runResults().clear();
            }
        }
    }

    public void runTests(int repeats, int sleepBetweenMs) {
        final Benchmark bmRoot = new Benchmark();

        bmRoot.print("Running warmups", warmupFns.size());
        clearResults(warmupFns);
        bmRoot.print("Running warmup functions");
        runRunnableFns(warmupFns, true, 0, false);
        bmRoot.print("Running warmups for tested functions with", repeats / 2, "repeats");
        runTestRoundsWithLogging(repeats / 2, sleepBetweenMs, bmRoot);
        bmRoot.print("Results for warmup");
        createRunSummary(runnableFns, repeats/2).printResults();
        bmRoot.print("Warmup ready.");
        bmRoot.print("Sleeping for", formatDuration(sleepBetweenMs));
        System.out.println("----------------------");
        gcAndSleep(sleepBetweenMs);
        bmRoot.print("Starting test round with", repeats, "repeats");
        runTestRoundsWithLogging(repeats, sleepBetweenMs, bmRoot);

        System.out.println("----------------------");
        bmRoot.print("Ready. Showing results");
        createRunSummary(runnableFns, repeats).printResults();
    }

    private void runTestRoundsWithLogging(int repeats, int sleepBetweenMs, Benchmark bmRoot) {
        Thread loggerThread = null;
        try {
            loggerThread = Thread.ofVirtual().start(timedLogger(repeats, bmRoot));
            runTestRounds(repeats, sleepBetweenMs);
        } finally {
            if (loggerThread != null) {
                loggerThread.interrupt();
            }
        }

    }

    private static void gcAndSleep(int sleepBetweenMs) {
        System.gc();
        sleepMs(sleepBetweenMs);
    }

    private void runTestRounds(int repeats, int sleepBetweenMs) {
        for (int i = 1; i <= repeats; i++) {
            currentTestRound = i;
            testRoundFns.clear();
            testRoundFns.addAll(runnableFns);
            Collections.shuffle(testRoundFns);
            runRunnableFns(testRoundFns, false, sleepBetweenMs, true);
        }
        currentTestRound = null;
        testRoundFns.clear();
    }

    @NotNull
    private Runnable timedLogger(int repeats, Benchmark bmRoot) {
        return () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (!testRoundFns.isEmpty()) {
                        final var tests = testRoundFns.stream().map(RunnableFn::name).toList();
                        bmRoot.print("Running round", currentTestRound, "of", repeats, "Tests to be run "+tests.size() + ":", StringHelpers.joinStrings(tests.toArray()));
                    }
                    Thread.sleep(Duration.ofSeconds(loggingIntervalSeconds));
                }
            } catch (InterruptedException e) {
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
    }

    private <P, R> RunSummary createRunSummary(List<RunnableFn> runnableFns, int repeats) {
        final var results = new ArrayList<RunResultSummary>();
        for (RunnableFn<P, R> fn : runnableFns) {
            final var durations = fn.runResults().stream().map(RunResult::duration).toList();
            double mean = calculateMean(durations);
            double standardDeviation = calculateStandardDeviation(durations);
            results.add(new RunResultSummary(
                    fn.name,
                    durations,
                    findMinMax(durations),
                    mean,
                    calculatePercentile(durations, 75.0),
                    standardDeviation,
                    standardDeviation / mean * 100.0));
        }
        results.sort(Comparator.comparing(RunResultSummary::mean));
        return new RunSummary(results, repeats);
    }

    private static void sleepMs(int ms) {
        if (ms > 0) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public record MinMax(long min, long max) {

    }

    static MinMax findMinMax(List<Long> values) {
        Long min = null, max = null;
        for (var value : values) {
            if (min == null) {
                min = max = value;
            }
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }
        return new MinMax(min, max);
    }

    static double calculateMean(List<Long> values) {
        int size = values.size();
        double sum = 0;
        for (var value : values) {
            sum += value;
        }
        return sum / size;
    }

    static double calculateStandardDeviation(List<Long> values) {
        final var size = values.size();
        if (size < 2) {
            throw new IllegalArgumentException("Two values are required to calculate standard deviation.");
        }

        final var mean = calculateMean(values);

        // Sum of squared differences from the mean
        double sumOfSquaredDifferences = 0;
        for (double value : values) {
            sumOfSquaredDifferences += Math.pow(value - mean, 2);
        }

        // Standard deviation
        return Math.sqrt(sumOfSquaredDifferences / size);
    }

    public static double calculatePercentile(List<Long> values, double percentile) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("List of values cannot be null or empty.");
        }

        // Sort the list of values
        final var sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);

        // Calculate the index of the percentile value
        double index = (percentile / 100.0) * (sortedValues.size() - 1);

        // If the index is an integer, return the corresponding value
        if (index % 1 == 0) {
            return sortedValues.get((int) index);
        } else {
            // If the index is not an integer, interpolate between values
            int lowerIndex = (int) Math.floor(index);
            int upperIndex = (int) Math.ceil(index);
            double lowerValue = sortedValues.get(lowerIndex);
            double upperValue = sortedValues.get(upperIndex);
            return lowerValue + (index - lowerIndex) * (upperValue - lowerValue);
        }
    }

    public int testRunCount() {
        return runnableFns.size();
    }
}
