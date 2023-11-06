package fi.iki.apo.pmap;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static fi.iki.apo.pmap.LoadGenerator.listOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PMapTest {
    @Test
    public void pmapBatching() {
        for(int c = 0; c < 1000; c++) {
            var list = listOf(c, integer -> integer);
            Function<Integer, String> integerStringFunction = i -> "a" + i;
            var expected = JavaMapAlternatives.mapFastest(list, integerStringFunction);
            List<String> actual = PartitionedOpsPerThreadMap.pmapPartitionModuloFixedReused(list, integerStringFunction);
            assertEquals(expected, actual);
            List<String> actual1 = PartitionedOpsPerThreadMap.pmapPartitionSegmentFixedReused(list, integerStringFunction);
            assertEquals(expected, actual1);
            List<String> actual2 = PartitionedOpsPerThreadMap.pmapPartitionSegmentFJ(list, integerStringFunction);
            assertEquals(expected, actual2);
            List<String> actual3 = PartitionedOpsPerThreadMap.pmapPartitionModuloFJ(list, integerStringFunction);
            assertEquals(expected, actual3);
        }
    }
}