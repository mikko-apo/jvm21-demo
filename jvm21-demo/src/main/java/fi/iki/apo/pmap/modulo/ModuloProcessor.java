package fi.iki.apo.pmap.modulo;

import fi.iki.apo.pmap.JavaMapAlternatives;
import fi.iki.apo.pmap.TasksAndArray;
import fi.iki.apo.pmap.forkjoinpool.ForkJoinProcessTask;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static fi.iki.apo.pmap.JavaMapAlternatives.getCpuCount;
import static fi.iki.apo.pmap.MultipleOpsPerThreadMap.executeTasks;
import static fi.iki.apo.pmap.MultipleOpsPerThreadMap.executeTasksInFJP;

public class ModuloProcessor {
    public <T, R> List<R> pmapModuloFixedReused(List<T> list, Function<T, R> f) {
        final var tasksAndResultArray = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> (Callable<Boolean>) () -> {
            mapWithModulo(list, arr, startIndex, jumpSize, f);
            return true;
        });
        return executeTasks(JavaMapAlternatives.reusedVirtualFixedThreadPool, tasksAndResultArray);
    }

    public  <T, R> List<R> pmapModuloFixed(List<T> list, Function<T, R> f) {
        final var tasksAndResultArray = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> (Callable<Boolean>) () -> {
            mapWithModulo(list, arr, startIndex, jumpSize, f);
            return true;
        });
        try (final var executorService = Executors.newFixedThreadPool(getCpuCount())) {
            return executeTasks(executorService, tasksAndResultArray);
        }
    }

    public <T, R> List<R> pmapModuloFJ(List<T> list, Function<T, R> f) {
        final var result = TasksAndArray.createModuloTasks(list.size(), (arr, startIndex, jumpSize) -> new ForkJoinProcessTask(null, () -> mapWithModulo(list, arr, startIndex, jumpSize, f)));
        return executeTasksInFJP(result);
    }

    private static <T, R> void mapWithModulo(List<T> list, Object[] rArr, int startIndex, int jump, Function<T, R> f) {
        int size = list.size();
        int index = startIndex;
        while (index < size) {
            rArr[index] = f.apply(list.get(index));
            index += jump;
        }
    }

}
