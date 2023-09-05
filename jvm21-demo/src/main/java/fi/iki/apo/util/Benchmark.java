package fi.iki.apo.util;

public class Benchmark {
    private final long start = System.currentTimeMillis();
    public class Duration {
        public final long durationMs;

        public Duration(long durationMs) {
            this.durationMs = durationMs;
        }
        public void print(Object... arr) {
            printDurationLog(durationMs, arr);
        }
    }

    public void print(Object... arr) {
        System.out.println(createLog(arr));
    }

    private String createLog(Object... arr) {
        return printDurationLog(calculateDuration(), arr);
    }

    private static String printDurationLog(long duration, Object... arr) {
        return formatDuration(duration) + " - " + StringHelpers.joinStrings(arr);
    }

    public static String formatDuration(long duration) {
        if (duration < 1000) {
            return duration + "ms";
        }
        return String.format("%.3f", duration / 1000.0);
    }

    private long calculateDuration() {
        return System.currentTimeMillis() - start;
    }

    public Duration getDuration() {
        return new Duration(calculateDuration());
    }
}
