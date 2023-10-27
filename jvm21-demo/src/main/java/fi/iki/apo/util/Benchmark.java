package fi.iki.apo.util;

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

    public long calculateDuration() {
        return System.currentTimeMillis() - start;
    }

    public Duration getDuration() {
        return new Duration(calculateDuration());
    }
}
