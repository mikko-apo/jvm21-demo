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
}
