package fi.iki.apo.pmap

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.*

fun <T, R> List<T>.mapListConstructor(f: (t: T) -> R): List<R> {
    return List(size) { index -> f(get(index)) }
}

fun <T, R> List<T>.mapFor(f: (t: T) -> R): List<R> {
    val list = mutableListOf<R>()
    for (t in this) {
        list.add(f(t))
    }
    return list
}

fun <T, R> List<T>.mapWithoutThreads(f: (t: T) -> R): List<R> {
    return this.map { f(it) }
}

fun <A, B> List<A>.pmapCoroutines(f: (A) -> B): List<B> = runBlocking {
    val deferred = map { async { f(it) } }
    deferred.map { it.await() }
}

// code adapted from https://kt.academy/article/cc-recipes
fun <T, R> Iterable<T>.mapAsync(
    transformation: (T) -> R
): List<R> = runBlocking {
    this@mapAsync
        .map { async { transformation(it) } }
        .awaitAll()
}

// code adapted  from https://kt.academy/article/cc-recipes
fun <T, R> Iterable<T>.mapAsync(
    concurrency: Int,
    transformation: (T) -> R
): List<R> = runBlocking {
    val semaphore = Semaphore(concurrency)
    this@mapAsync
        .map { async { semaphore.withPermit { transformation(it) } } }
        .awaitAll() as List<R>
}

fun  <T, R> Iterable<T>.pmapThreadPoolCoroutines(transformation: (T) -> R): List<R>  {
   val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
   val dispatcher = threadPool.asCoroutineDispatcher()
    return runBlocking(dispatcher) {
        this@pmapThreadPoolCoroutines
            .map { async { transformation(it) } }
            .awaitAll()
    }
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
