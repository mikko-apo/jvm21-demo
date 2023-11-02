package fi.iki.apo.pmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LoadGenerator {
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
    }

    public static int powSqrt(int i) {
        return (int) Math.pow(Math.sqrt(i+1)+i, 2);
    }

    public static <T> List<T> listOf(int i, Function<Integer, T> f) {
        final var arr = new ArrayList<T>(i);
        for (int c = 0; c < i; c++) {
            arr.add(f.apply(c));
        }
        return arr;
    }
}
