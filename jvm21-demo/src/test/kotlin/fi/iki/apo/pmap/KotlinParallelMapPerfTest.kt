package fi.iki.apo.pmap

import fi.iki.apo.pmap
import fi.iki.apo.pmap.*
import fi.iki.apo.util.Benchmark
import org.junit.jupiter.api.Test
import java.time.Duration

class KotlinParallelMapPerfTest {
    var results: Long = 0

    @Test
    fun perfFastLooper() {
        results += performanceTest(
            16,
            listOfInts(1000000),
            KotlinLoadGenerator::looperFast
        )
    }

    @Test
    fun perfSlowLooper() {
        results += performanceTest(
            8,
            listOfInts(1000000),
            KotlinLoadGenerator::looperSlow
        )
    }

    @Test
    fun perfWithLongPrimitiveInOut() {
        results += performanceTest(
            8,
            listOfLongs(1000000),
            KotlinLoadGenerator::looperSlowLongParameter
        )
    }

    private val listOfInts = { size: Int -> List(size) { i -> i } }
    private val listOfLongs = { size: Int -> List(size) { i -> i.toLong() } }

    private fun sleep1() {
        Thread.sleep(Duration.ofSeconds(1))
    }

    private fun <T, R> performanceTest(repeats: Int, items: List<T>, testF: (T) -> R): Long {
        var results: Long = 0
        val bm = Benchmark()
        bm.print("Warming load function")
        val warmupItems = items.take(items.size / 10)
        val warmUpResults = warmupItems.map(testF)
        results += warmUpResults.size.toLong()
        bm.print("Warming tested functions with", warmupItems.size, "items")
        results += executePerformanceTests(repeats, warmupItems, testF, false)
        bm.print("Warming up done. Sleeping 1 second ", items.size, "items")
        sleep1();
        println("----------------------")
        results += executePerformanceTests(repeats, items, testF, true)
        println("----------------------")
        bm.print("Tests done")
        return results
    }


    private fun <T, R> executePerformanceTests(
        repeats: Int,
        items: List<T>,
        testF: (T) -> R,
        showResult: Boolean
    ): Long {
        var results: Long = 0
        val benchmarkMap = { name: String, mapF: (list: List<T>, f: (t: T) -> R) -> List<R> ->
            Benchmark.benchmarkList(items, repeats, showResult, name) { l -> mapF(l, testF) }.size
        }

        results += benchmarkMap("map with Kotlin list.map{}", List<T>::mapWithoutThreads)
        sleep1()

        results += benchmarkMap("pmap with Kotlin fixedVirtualThreads", List<T>::pmapFixedVirtualThreadPool)
        sleep1()

        results += benchmarkMap("pmap with Kotlin newFixedThreadPool", List<T>::pmap)
        sleep1()

        results += benchmarkMap("pmap with Kotlin newVirtualThreadPerTaskExecutor", List<T>::pmapVirtualThreads)
        sleep1()

        results += benchmarkMap("pmap with Kotlin Coroutines", List<T>::pmapCoroutines)
        return results
    }
}