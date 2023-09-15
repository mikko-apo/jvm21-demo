package fi.iki.apo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CollectionHelpers {
    public static <I, K> Map<K, List<I>> groupBy(List<I> list, Function<I, K> keyFn) {
        final var map = new HashMap<K, List<I>>();
        list.forEach(i -> {
            var key = keyFn.apply(i);
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(i);
        });
        return map;
    }

    public static <I> SplitResult<I> split(List<I> list, Function<I, Boolean> splitFn) {
        return new SplitResult<>(list, splitFn);
    }

    public static class SplitResult<I> {
        public final List<I> good = new ArrayList<>();
        public final List<I> bad = new ArrayList<>();

        public SplitResult(List<I> list, Function<I, Boolean> splitFn) {
            list.forEach(i -> {
                if (splitFn.apply(i)) {
                    good.add(i);
                } else {
                    bad.add(i);
                }
            });
        }
    }
}
