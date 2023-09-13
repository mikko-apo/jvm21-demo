package fi.iki.apo.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringHelpers {
    public static String resolveErrorDescription(Throwable t) {
        String message = t.getMessage();
        if (message != null) {
            return message;
        }
        return t.getClass().getName();
    }

    public static String joinStringsWithDelimiter(String delimiter, Object... arr) {
        return Arrays.stream(arr).map(o -> o != null ? o.toString() : "null").collect(Collectors.joining(delimiter));
    }

    public static String joinStrings(Object... arr) {
        return joinStringsWithDelimiter(" ", arr);
    }

    public static String formatDuration(long duration) {
        if (duration < 1000) {
            return duration + "ms";
        }
        return String.format("%.3f", duration / 1000.0);
    }

    public static String formatMemory(long memory) {
        final int kb = 1024;
        final int mb = kb*kb;
        final int gb = kb*kb*kb;
        final var absMemory = Math.abs(memory);
        if(absMemory > gb) {
            return String.format("%.2fGB", memory / (double)gb);
        }
        if(absMemory > mb) {
            return String.format("%.2fMB", memory / (double)mb);
        }
        if(absMemory > kb) {
            return String.format("%.2fKB", memory / (double)mb);
        }
        return memory +"B";
    }

}
