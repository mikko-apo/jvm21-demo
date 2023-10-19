package fi.iki.apo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static fi.iki.apo.util.StringHelpers.formatDuration;

public class Benchmark {
    private final long start = System.currentTimeMillis();

    public record Duration(long durationMs) {

    }

    public void print(Object... arr) {
        System.out.println(createLog(arr));
    }

    private String createLog(Object... arr) {
        return formatDurationLog(calculateDuration(), arr);
    }

    private static String formatDurationLog(long duration, Object... arr) {
        return formatDuration(duration) + " - " + StringHelpers.joinStrings(arr);
    }

    private long calculateDuration() {
        return System.currentTimeMillis() - start;
    }

    public Duration getDuration() {
        return new Duration(calculateDuration());
    }

    public static <T, R> List<R> benchmarkList(List<T> items, int repeats, boolean showResult, String name, Function<List<T>, R> f) {
        final var bmRoot = new Benchmark();
        final var results = new ArrayList<R>(items.size());
        final var durations = new ArrayList<Long>();
        for (int i = 0; i < repeats; i++) {
            final var bm = new Benchmark();
            results.add(f.apply(items));
            durations.add(bm.calculateDuration());
        }
        if (showResult) {
            final var minMax = findMinMax(durations);
            double standardDeviation = calculateStandardDeviation(durations);
            double mean = calculateMean(durations);
            bmRoot.print(
                    name,
                    repeats,
                    "repeats:",
                    "75% percentile",
                    formatDuration((long)calculatePercentile(durations, 75.0)),
                    "[",
                    formatDuration(minMax.min),
                    "-",
                    formatDuration(minMax.max),
                    "]",
                    "average",
                    formatDuration((long) mean),
                    "std dev",
                    String.format("%.2f%%", standardDeviation / mean * 100.0),
                    formatDuration((long) standardDeviation)
            );
        }
        return results;
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
}
