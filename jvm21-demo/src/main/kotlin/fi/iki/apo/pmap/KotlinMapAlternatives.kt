package fi.iki.apo.pmap

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.*

fun <T, R> List<T>.mapWithoutThreads(f: (t: T) -> R): List<R> {
    return this.map { f(it) }
}

fun <A, B> List<A>.pmapCoroutines(f: (A) -> B): List<B> = runBlocking {
    val deferred = map { async { f(it) } }
    deferred.map { it.await() }
}

fun <T, R> List<T>.pmapVirtualThreads(f: (t: T) -> R): List<R> {
    val tasks = map { t -> Callable { f(t) } }
    val futures = Executors.newVirtualThreadPerTaskExecutor().use { executorService ->
        executorService.invokeAll(tasks)
    }
    return futures.map { it.get() }
}

fun <T, R> List<T>.pmapFixedVirtualThreadPool(f: (t: T) -> R): List<R> {
    val tasks = map { t -> Callable { f(t) } }
    val cpus = Runtime.getRuntime().availableProcessors()
    val pool = ThreadPoolExecutor(
        cpus,
        cpus,
        0L,
        TimeUnit.MILLISECONDS,
        LinkedBlockingQueue(),
        Thread.ofVirtual().factory(),
        ThreadPoolExecutor.AbortPolicy()
    );
    val futures = pool.use { executorService ->
        executorService.invokeAll(tasks)
    }
    return futures.map { it.get() }
}

