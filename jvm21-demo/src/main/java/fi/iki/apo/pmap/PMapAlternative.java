package fi.iki.apo.pmap;

import java.util.List;
import java.util.function.Function;

public interface PMapAlternative {
    <T, R> List<R> pmap(List<T> list, Function<T, R> f);
}
