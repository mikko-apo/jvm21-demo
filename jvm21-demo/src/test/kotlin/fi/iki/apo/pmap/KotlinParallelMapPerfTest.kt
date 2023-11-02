package fi.iki.apo.pmap

import fi.iki.apo.pmap
import fi.iki.apo.util.PerfTest
import org.junit.jupiter.api.Test

class KotlinParallelMapPerfTest {
    var results: Long = 0

    private val testItemCount = 1000000

    @Test
    fun perfFastLooper() {
        results += executePerformanceTests(
            16,
            testItemCount,
            KotlinLoadGenerator::listOfInts,
            KotlinLoadGenerator::looperFast
        )
    }

    @Test
    fun perfSlowLooper() {
        results += executePerformanceTests(
            8,
            testItemCount,
            KotlinLoadGenerator::listOfInts,
            KotlinLoadGenerator::looperSlow
        )
    }

    @Test
    fun perfMathPowSqrtFast() {
        results += executePerformanceTests(
            8,
            testItemCount / 10,
            KotlinLoadGenerator::listOfInts,
            KotlinLoadGenerator::powSqrt
        )
    }

    @Test
    fun perfMathPowSqrtSlow() {
        results += executePerformanceTests(
            8,
            testItemCount / 10,
            KotlinLoadGenerator::listOfInts,
            KotlinLoadGenerator::powSqrt
        )
    }

    private fun <T, R> executePerformanceTests(
        repeats: Int, testItemCount: Int, testDataBuilder: (Int) -> List<T>, testF: (T) -> R
    ): Int {
        val perf = PerfTest(testDataBuilder, testItemCount, testItemCount / 10)

        perf.addWarmup("testF") { list: List<T> ->
            list.map { testF(it) }.size
        }

        perf
            .addTestRun("map with Kotlin list.map{}") { list -> list.mapWithoutThreads(testF).size }
            .addTestRun("map with Kotlin List(n){}") { list -> list.mapListConstructor(testF).size }
            .addTestRun("map with Kotlin for(t : list)") { list -> list.mapFor(testF).size }
            .addTestRun("pmap with Kotlin fixedVirtualThreads") { list -> list.pmapFixedVirtualThreadPool(testF).size }
            .addTestRun("pmap with Kotlin newFixedThreadPool") { list -> list.pmap(testF).size }
            .addTestRun("pmap with Kotlin newVirtualThreadPerTaskExecutor") { list -> list.pmapNewVirtualThread(testF).size }
            .addTestRun("pmap with Kotlin Coroutines") { list -> list.pmapCoroutines(testF).size }
            .addTestRun("pmap with Kotlin Coroutines mapAsync") { list -> list.pmapCoroutinesCC(testF).size }
            .addTestRun("pmap with Kotlin Coroutines mapAsync semaphore") { list -> list.pmapCoroutinesCCSemaphore(testF).size }
            .addTestRun("pmap with Kotlin pmapThreadPoolCoroutines") { list -> list.pmapCoroutinesThreadPool(testF).size }

        perf.runTests(repeats, 1000)
        return perf.testRunCount()
    }
}