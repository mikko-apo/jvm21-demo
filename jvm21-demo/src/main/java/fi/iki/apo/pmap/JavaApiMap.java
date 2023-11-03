package fi.iki.apo.pmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JavaApiMap {
    public static <T, R> List<R> mapStream(List<T> list, Function<T, R> f) {
        return list.stream().map(f).toList();
    }

    public static <T, R> List<R> pmapParallelStream(List<T> list, Function<T, R> f) {
        return list.parallelStream().map(f).toList();
    }

    public static <T, R> List<R> mapFor(List<T> list, Function<T, R> f) {
        final var dest = new ArrayList<R>(list.size());
        for (T t : list) {
            dest.add(f.apply(t));
        }
        return dest;
    }
}
