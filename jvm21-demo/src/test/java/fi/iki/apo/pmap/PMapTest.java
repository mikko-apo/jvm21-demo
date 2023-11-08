package fi.iki.apo.pmap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static fi.iki.apo.pmap.LoadGenerator.listOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PMapTest {
    @Test
    public void pmapBatching() {
        for (int c = 0; c < 1000; c++) {
            var list = listOf(c, integer -> integer);
            Function<Integer, String> integerStringFunction = i -> "a" + i;
            var expected = JavaMapAlternatives.mapFastest(list, integerStringFunction);
            assertEquals(expected, MultipleOpsPerThreadMap.pmapModuloFixedReused(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessorCpu.pmapBlockFixedReusedVT(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessorCpu.pmapBlockFJ(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.pmapModuloFJ(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessor2000.pmapBlockFJ(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessor2000.pmapBlockFixed(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessor2000.pmapBlockFixedReused(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockProcessor2000.pmapBlockFixedReusedVT(list, integerStringFunction));
        }
    }

    @Test
    public void blockRange() {
        assertEquals(Arrays.asList(), BlockRange.splitByBlockSize(0, 0));
        assertEquals(Arrays.asList(), BlockRange.splitByBlockSize(0, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 0)), BlockRange.splitByBlockSize(1, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 0), new BlockRange(1, 1)), BlockRange.splitByBlockSize(2, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 1), new BlockRange(2, 2)), BlockRange.splitByBlockSize(3, 2));
    }
}
