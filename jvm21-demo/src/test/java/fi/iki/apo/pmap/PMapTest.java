package fi.iki.apo.pmap;

import fi.iki.apo.pmap.block.BlockRange;
import fi.iki.apo.pmap.block.BlockRangeFactory;
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
            assertEquals(expected, MultipleOpsPerThreadMap.modulo.pmapModuloFixedReused(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockCountCpu.reusedVirtualFixedThreadPool.pmap(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockCountCpu.commonFJPool.pmap(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.modulo.pmapModuloFJ(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockSize2000.commonFJPool.pmap(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockSize2000.pmapBlockFixed(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockSize2000.reusedFixedThreadPool.pmap(list, integerStringFunction));
            assertEquals(expected, MultipleOpsPerThreadMap.blockSize2000.reusedVirtualFixedThreadPool.pmap(list, integerStringFunction));
        }
    }

    @Test
    public void blockRange() {
        assertEquals(Arrays.asList(), BlockRangeFactory.splitByBlockSize(0, 0));
        assertEquals(Arrays.asList(), BlockRangeFactory.splitByBlockSize(0, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 0)), BlockRangeFactory.splitByBlockSize(1, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 0), new BlockRange(1, 1)), BlockRangeFactory.splitByBlockSize(2, 1));
        assertEquals(Arrays.asList(new BlockRange(0, 1), new BlockRange(2, 2)), BlockRangeFactory.splitByBlockSize(3, 2));
    }

    @Test
    public void otherParalleFunctions() {
        final var processor = MultipleOpsPerThreadMap.blockSize2000.reusedFixedThreadPool;
        for (int c = 0; c < 1000; c++) {
            var list = listOf(c, integer -> integer);
            assertEquals(list.stream().filter(integer -> integer % 2 == 0).toList(), processor.filter(list, integer -> integer % 2 == 0));
            assertEquals(list.stream().reduce(0, (a, b) -> a + b*2), processor.mapReduce(list, integer -> integer * 2, lists -> {
                int counter = 0;
                for (var l : lists) {
                    for (var i : l) {
                        counter += i;
                    }
                }
                return counter;
            }));
        }

    }
}
