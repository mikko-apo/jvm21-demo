package fi.iki.apo.pmap;

public class LoadGenerator {
    public static long looperSlowLongParameter(long i) {
        int c = 0;
        long counter = 0;
        int endIndex = (int)i;
        while (c < endIndex) {
            counter += 1;
            c += 1;
        }
        return counter;
    }

    public static int looperFast(int i) {
        int c = 0;
        int counter = 0;
        while (c < i) {
            counter += 1;
            c += 1;
        }
        return counter;
    }

    public static int looperSlow(int i) {
        int c = 0;
        long counter = 0;
        while (c < i) {
            counter += 1;
            c += 1;
        }
        return (int) counter;
    }}
