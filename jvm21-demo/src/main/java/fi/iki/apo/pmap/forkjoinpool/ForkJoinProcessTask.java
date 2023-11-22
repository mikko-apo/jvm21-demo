package fi.iki.apo.pmap.forkjoinpool;

import java.util.List;
import java.util.concurrent.RecursiveAction;

public class ForkJoinProcessTask extends RecursiveAction {

    private final List<ForkJoinProcessTask> tasks;
    private final Runnable runnable;

    public ForkJoinProcessTask(List<ForkJoinProcessTask> tasks, Runnable runnable) {
        this.tasks = tasks;
        this.runnable = runnable;
    }

    @Override
    protected void compute() {
        if (tasks != null) {
            invokeAll(tasks);
        } else {
            runnable.run();
        }
    }
}
