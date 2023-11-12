package fi.iki.apo.pmap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class JavaMapAlternatives {
    public static <T, R> List<R> mapFastest(List<T> list, Function<T, R> f) {
        return JavaApiMap.pmapParallelStream(list, f);
    }

    static int getCpuCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    static final ThreadPoolExecutor reusedVirtualFixedThreadPool = new ThreadPoolExecutor(
            getCpuCount(),
            getCpuCount(),
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(),
            Thread.ofVirtual().factory(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    static final ThreadPoolExecutor reusedVirtualFixedThreadPoolDoubleThreads = new ThreadPoolExecutor(
            getCpuCount()*2,
            getCpuCount()*2,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(),
            Thread.ofVirtual().factory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    static final ExecutorService reusedFixedThreadPool = Executors.newFixedThreadPool(getCpuCount());
    static final ExecutorService reusedFixedThreadPoolDoubleThreads = Executors.newFixedThreadPool(getCpuCount()*2);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            reusedVirtualFixedThreadPool.shutdownNow();
            reusedFixedThreadPool.shutdownNow();
        }));
    }

}
