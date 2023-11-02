package fi.iki.apo.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.lang.reflect.Type


fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val paths = listOf("pmap-java-tests/jmh-result.json", "pmap-kotlin-tests/jmh-result.json")

    mergeAndPrint(paths) { testName ->
        testName.multiReplace(mapOf("fi.iki.apo." to "", "PmapBenchmark" to ""))
    }
}

private fun mergeAndPrint(paths: List<String>, testNameFormatter: (String) -> String = { s -> s }) {
    val gsonBuilder = GsonBuilder()
    gsonBuilder.registerTypeAdapter(Time::class.java, TimeDeserializer())
    val gson = gsonBuilder.create()
    val typeToken = object : TypeToken<List<JmhResult>>() {}.type
    val jsonData = paths.map { gson.fromJson<List<JmhResult>>(FileReader(it), typeToken) }
    val merged = jsonData.fold(listOf<JmhResult>()) { acc, jmhResults -> acc + jmhResults }
    val groupByParams = merged.groupBy { it.params }
    groupByParams.forEach { (params, items) ->
        println(params.toString())
        val sortedAndNamed =
            items.sortedBy { it.primaryMetric.score }.map { it.copy(benchmark = testNameFormatter(it.benchmark)) }
        val longestBenchmarkNameLength = sortedAndNamed.max { it.benchmark.length } ?: 0
        val first = sortedAndNamed[0]
        sortedAndNamed.forEach { item ->
            val score = item.primaryMetric.score
            val scoreError = item.primaryMetric.scoreError
            val percentageOfFirst = score / first.primaryMetric.score
            val relativeError = scoreError / score * 100
            println(
                "%-${longestBenchmarkNameLength + 1}s %9.3f ± %4.1f%% (%8.3f) %s - %.1fx".format(
                    testNameFormatter(item.benchmark),
                    score,
                    relativeError,
                    scoreError,
                    item.primaryMetric.scoreUnit,
                    percentageOfFirst
                )
            )
        }
    }
}

fun String.multiReplace(map: Map<String, String>) =
    map.entries.fold(this) { acc, (key, dest) -> acc.replace(key, dest) }

fun <T> List<T>.max(f: (T) -> Int): Int? = fold(null) { acc: Int?, t ->
    val i = f(t)
    if (acc == null || i > acc) i else acc
}

data class JmhResult(
    val jmhVersion: String,
    val benchmark: String,
    val mode: String,
    val threads: Long,
    val forks: Long,
    val jvm: String,
    val jvmArgs: List<Any?>,
    val jdkVersion: String,
    val vmName: String,
    val vmVersion: String,
    val warmupIterations: Long,
    val warmupTime: Time?,
    val warmupBatchSize: Long,
    val measurementIterations: Long,
    val measurementTime: Time?,
    val measurementBatchSize: Long,
    val params: Map<String, String>,
    val primaryMetric: PrimaryMetric,
    val secondaryMetrics: Map<String, String>
)

class TimeDeserializer : JsonDeserializer<Time> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Time? {
        val timeString = json?.asString
        return if (timeString != null) Time(timeString) else null
    }
}

data class Time(val value: String) {
}

data class PrimaryMetric(
    val score: Double,
    val scoreError: Double,
    val scoreConfidence: List<Double>,
    val scorePercentiles: Map<String, Double>,
    val scoreUnit: String,
    val rawData: List<List<Double>>
)