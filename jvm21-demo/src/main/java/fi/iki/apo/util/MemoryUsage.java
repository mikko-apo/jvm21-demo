package fi.iki.apo.util;

import static fi.iki.apo.util.StringHelpers.formatMemory;
import static fi.iki.apo.util.StringHelpers.joinStrings;

public class MemoryUsage {
    private final long startTotal = Runtime.getRuntime().totalMemory();
    private final long startFree = Runtime.getRuntime().freeMemory();

    public String format() {
        final long currentlyUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return joinStrings("Memory use changes",
                formatMemory(currentlyUsed),
                "(change:",
                formatMemory(currentlyUsed - (startTotal - startFree))+
                ") total:",
                formatMemory(Runtime.getRuntime().totalMemory())
        );
    }
}
