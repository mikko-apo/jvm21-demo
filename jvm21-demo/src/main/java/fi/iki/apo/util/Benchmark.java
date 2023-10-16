package fi.iki.apo.util;

import java.util.ArrayList;
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
        for (int i = 1; i <= repeats; i++) {
            results.add(f.apply(items));
        }
        if (showResult) {
            long averageDurationMs = (long) (bmRoot.calculateDuration() / (double) repeats);
            bmRoot.print(name, "average duration per", repeats, "repeats", formatDurationLog(averageDurationMs));
        }
        return results;
    }
}
