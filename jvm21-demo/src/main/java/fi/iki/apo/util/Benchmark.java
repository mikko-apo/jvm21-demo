package fi.iki.apo.util;

import static fi.iki.apo.util.StringHelpers.formatDuration;

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

    private long calculateDuration() {
        return System.currentTimeMillis() - start;
    }

    public Duration getDuration() {
        return new Duration(calculateDuration());
    }
}
