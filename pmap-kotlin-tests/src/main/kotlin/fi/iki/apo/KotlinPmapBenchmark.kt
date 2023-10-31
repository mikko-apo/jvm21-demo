package fi.iki.apo

import fi.iki.apo.pmap.*
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@Warmup(iterations = 16)
@Measurement(iterations = 16)
@BenchmarkMode(Mode.AverageTime)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
@Fork(value = 1, warmups = 0)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
open class KotlinPmapBenchmark {

    @Param(FAST, SLOW)
    open var loadGeneratorType: String? = null

    private var items: List<Int> = listOf()

    @Setup(Level.Invocation)
    fun setup() {
        items = KotlinLoadGenerator.listOfInts(1000000)
    }

    private fun resolveLoadGenerator(): (i: Int) -> Int {
        return when (loadGeneratorType) {
            FAST -> KotlinLoadGenerator::looperFast
            SLOW -> KotlinLoadGenerator::looperSlow
            else -> throw RuntimeException("Unsupported LoadGenerator type $loadGeneratorType")
        }
    }

    companion object {
        const val FAST = "fast"
        const val SLOW = "slow"
    }

    @Benchmark
    fun mapWithoutThreads() = items.mapWithoutThreads(resolveLoadGenerator())

    @Benchmark
    fun mapListConstructor() = items.mapListConstructor(resolveLoadGenerator())

    @Benchmark
    fun mapFor() = items.mapFor(resolveLoadGenerator())

    @Benchmark
    fun pmapFixedVirtualThreadPool() = items.pmapFixedVirtualThreadPool(resolveLoadGenerator())

    @Benchmark
    fun pmapFixedThreadPool() = items.pmap(resolveLoadGenerator())

    @Benchmark
    fun pmapNewVirtualThread() = items.pmapNewVirtualThread(resolveLoadGenerator())

    @Benchmark
    fun pmapCoroutines() = items.pmapCoroutines(resolveLoadGenerator())

    @Benchmark
    fun pmapCoroutinesCC() = items.pmapCoroutinesCC(resolveLoadGenerator())

    @Benchmark
    fun pmapCoroutinesCCSemaphore() = items.pmapCoroutinesCCSemaphore(Runtime.getRuntime().availableProcessors(), resolveLoadGenerator())

    @Benchmark
    fun pmapCoroutinesThreadPool() = items.pmapCoroutinesThreadPool(resolveLoadGenerator())
}
