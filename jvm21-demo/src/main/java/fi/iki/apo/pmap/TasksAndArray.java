package fi.iki.apo.pmap;

import fi.iki.apo.pmap.block.BlockRange;

import java.util.ArrayList;
import java.util.List;

import static fi.iki.apo.pmap.JavaMapAlternatives.getCpuCount;

public record TasksAndArray<M>(List<M> tasks, Object[] rArr) {
    public static <M> TasksAndArray<M> createBlockTasks(int size, List<BlockRange> blockRanges, TriFunction<Object[], Integer, Integer, M> mapF) {
        final var rArr = new Object[size];
        final var tasks = JavaMapAlternatives.mapFastest(blockRanges, s -> mapF.apply(rArr, s.min(), s.max()));
        return new TasksAndArray<>(tasks, rArr);
    }

    public static <M> TasksAndArray<M> createModuloTasks(int size, TriFunction<Object[], Integer, Integer, M> mapUsingModulo) {
        final var rArr = new Object[size];
        int cpuCount = getCpuCount();
        final var tasks = new ArrayList<M>(cpuCount);
        for (int c = 0; c < cpuCount; c++) {
            tasks.add(mapUsingModulo.apply(rArr, c, cpuCount));
        }
        return new TasksAndArray<>(tasks, rArr);
    }
}
