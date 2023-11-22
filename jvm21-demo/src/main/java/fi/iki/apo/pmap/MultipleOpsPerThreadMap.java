package fi.iki.apo.pmap;

import fi.iki.apo.pmap.block.BlockProcessor;
import fi.iki.apo.pmap.forkjoinpool.ForkJoinProcessTask;
import fi.iki.apo.pmap.modulo.ModuloProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static fi.iki.apo.pmap.JavaMapAlternatives.getCpuCount;

public class MultipleOpsPerThreadMap {
    public static BlockProcessor blockCountCpu = new BlockProcessor(null, getCpuCount());
    public static BlockProcessor blockSize250 = new BlockProcessor(250, null);
    public static BlockProcessor blockSize500 = new BlockProcessor(500, null);
    public static BlockProcessor blockSize1000 = new BlockProcessor(1000, null);
    public static BlockProcessor blockSize2000 = new BlockProcessor(2000, null);
    public static BlockProcessor blockSize4000 = new BlockProcessor(4000, null);
    public static BlockProcessor blockSize8000 = new BlockProcessor(8000, null);
    public static BlockProcessor blockSize16000 = new BlockProcessor(16000, null);
    public static BlockProcessor blockSize32000 = new BlockProcessor(32000, null);

    public static <R> List<R> executeTasks(ExecutorService executor, TasksAndArray<Callable<Boolean>> tasksWithResultArray) {
        List<Callable<Boolean>> tasks = tasksWithResultArray.tasks();
        executeTasks(executor, tasks);
        return Arrays.asList((R[]) tasksWithResultArray.rArr());
    }

    public static void executeTasks(ExecutorService executor, List<Callable<Boolean>> tasks) {
        try {
            for (var future : executor.invokeAll(tasks)) {
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <R> List<R> executeTasksInFJP(TasksAndArray<ForkJoinProcessTask> result) {
        ForkJoinPool.commonPool().invoke(new ForkJoinProcessTask(result.tasks(), null));
        return Arrays.asList((R[]) result.rArr());
    }

    public static ModuloProcessor modulo = new ModuloProcessor();
}
