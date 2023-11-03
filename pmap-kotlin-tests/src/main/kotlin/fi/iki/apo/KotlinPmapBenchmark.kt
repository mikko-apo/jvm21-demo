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

    @Param(THOUSANDTHOUSAND, MILLION)
    open var itemCount: String? = null

    private var millionItems: List<Int> = listOf()
    private var thousandThousandItems: List<List<Int>> = listOf()

    @Setup(Level.Invocation)
    fun setup() {
        millionItems = KotlinLoadGenerator.listOfInts(1000000)
        thousandThousandItems = List(1000) {KotlinLoadGenerator.listOfInts(1000)}
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
        const val THOUSANDTHOUSAND = "1000*1000"
        const val MILLION = "1000000"
    }

    private fun runBenchmark(f: (List<Int>, (Int)->Int) -> List<Int>): List<Int> {
        val loadGenerator = resolveLoadGenerator()
        return when (itemCount) {
            MILLION -> f.invoke(KotlinLoadGenerator.listOfInts(1000000), loadGenerator)
            THOUSANDTHOUSAND -> {
                val results = mutableListOf<Int>()
                for(list in thousandThousandItems) {
                    results.addAll(f.invoke(list, loadGenerator))
                }
                results
            }
            else -> throw RuntimeException("Unsupported ItemCount $itemCount")
        }
    }

    @Benchmark
    fun mapWithoutThreads() = runBenchmark(List<Int>::mapWithoutThreads)

    @Benchmark
    fun mapListConstructor() = runBenchmark(List<Int>::mapListConstructor)

    @Benchmark
    fun mapFor() = runBenchmark(List<Int>::mapFor)

    @Benchmark
    fun pmapFixedVirtualThreadPool() = runBenchmark(List<Int>::pmapFixedVirtualThreadPool)

    @Benchmark
    fun pmapFixedThreadPool() = runBenchmark(List<Int>::pmap)

    @Benchmark
    fun pmapNewVirtualThread() = runBenchmark(List<Int>::pmapNewVirtualThread)

    @Benchmark
    fun pmapCoroutines() = runBenchmark(List<Int>::pmapCoroutines)

    @Benchmark
    fun pmapCoroutinesCC() = runBenchmark(List<Int>::pmapCoroutinesCC)

    @Benchmark
    fun pmapCoroutinesCCSemaphore() = runBenchmark(List<Int>::pmapCoroutinesCCSemaphore)

    @Benchmark
    fun pmapCoroutinesThreadPool() = runBenchmark(List<Int>::pmapCoroutinesThreadPool)

    @Benchmark
    fun pmapJavaParallelStreamMap() = runBenchmark { list, f -> JavaApiMap.pmapParallelStream(list, f)}

    @Benchmark
    fun pmapKotlinParallelStreamMap() = runBenchmark(List<Int>::pmapJavaStreamParallelMap)
}
