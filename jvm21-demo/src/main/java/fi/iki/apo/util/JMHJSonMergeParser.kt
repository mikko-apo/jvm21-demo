package fi.iki.apo.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.FileReader
import java.lang.reflect.Type


fun main(@Suppress("UNUSED_PARAMETER") args: Array<String>) {
    val paths = listOf("pmap-java-tests/jmh-result.json" /*, "pmap-kotlin-tests/jmh-result.json"*/)

    mergeAndPrint(paths) { testName ->
        testName.multiReplace(mapOf("fi.iki.apo." to "", "PmapBenchmark" to ""))
    }
}

private fun mergeAndPrint(paths: List<String>, testNameFormatter: (String) -> String = { s -> s }) {
    val gsonBuilder = GsonBuilder()
    gsonBuilder.registerTypeAdapter(Time::class.java, TimeDeserializer())
    val gson = gsonBuilder.create()
    val typeToken = object : TypeToken<List<JmhResult>>() {}.type
    val jsonData = paths.map { gson.fromJson<List<JmhResult>>(FileReader(it), typeToken) }.flatten()
    val data = convertResultsToAvg(jsonData)
    val merged = data.fold(listOf<JmhResult>()) { acc, jmhResults -> acc + jmhResults }
    val groupByParams = merged.groupBy { it.params }
    val groupedAndSortedAndRenamed = groupByParams.mapValues { (_, results) ->
        results.sortedBy { it.primaryMetric.score }.map { it.copy(benchmark = testNameFormatter(it.benchmark)) }
    }
    val groupedAndChecked = checkForMissingEntries(groupedAndSortedAndRenamed)
    val categoryMultipliers = calculateCategoryMultipliers(groupedAndChecked)
    printCategoryMultipliers(categoryMultipliers)
    printResultsByParamCategories(groupedAndChecked)
    printCombinedResultsWithScaling(groupedAndChecked, categoryMultipliers)
}

fun convertResultsToAvg(jsonData: List<JmhResult>): List<JmhResult> {
    return jsonData.map { result ->
        if (result.mode == "thrpt") {
            val m = result.primaryMetric
            val c = when (result.primaryMetric.scoreUnit) {
                "ops/s" -> ::toMsOps
                else -> throw Exception("Unsupported ${result.primaryMetric.scoreUnit}")
            }
            result.copy(
                mode = "avgt",
                primaryMetric = m.copy(
                    score = c(m.score),
                    scoreError = c(m.scoreError),
                    scoreConfidence = m.scoreConfidence.map(c),
                    scorePercentiles = m.scorePercentiles.mapValues { (k, v) -> c(v) },
                    rawData = m.rawData.map { it.map(c) }
                )
            )
        } else result
    }
}

fun toMsOps(d: Double) = 1 / d * 1000

fun checkForMissingEntries(groupByParams: Map<Map<String, String>, List<JmhResult>>): Map<Map<String, String>, List<JmhResult>> {
    val allBenchmarkNames = groupByParams.values.flatten().map { it.benchmark }.toSet()
    val paramsBenchmarkNames = groupByParams.mapValues { (_, results) -> results.map { it.benchmark }.toSet() }
    for ((key, benchmarkNames) in paramsBenchmarkNames) {
        val missing = allBenchmarkNames.filter { !benchmarkNames.contains(it) }
        if (missing.isNotEmpty()) {
            println("$key is missing: ${missing.joinToString(",")}")
        }
    }
    return allBenchmarkNames.fold(groupByParams) { acc, s ->
        val benchmarkInAllGroups = acc.values.all { list -> list.any { it.benchmark == s } }
        if (benchmarkInAllGroups)
            acc
        else
            acc.mapValues { (_, results) ->
                results.filter { it.benchmark != s }
            }
    }
}

fun printCategoryMultipliers(multipliers: Map<Map<String, String>, Double>) {
    println("*** Category multipliers")
    multipliers.forEach { (category, spread) ->
        println("%s %.1fx".format(category, spread))
    }
}

private fun printResultsByParamCategories(
    groupedAndSortedAndRenamed: Map<Map<String, String>, List<JmhResult>>
) {
    println("*** Results by categories")
    groupedAndSortedAndRenamed.forEach { (params, sortedAndNamed) ->
        println(params.toString())
        val longestBenchmarkNameLength = sortedAndNamed.max { it.benchmark.length } ?: 0
        val first = sortedAndNamed.first()
        sortedAndNamed.forEach { item ->
            val score = item.primaryMetric.score
            val scoreError = item.primaryMetric.scoreError
            val percentageOfFirst = score / first.primaryMetric.score
            val relativeError = scoreError / score * 100
            println(
                "%-${longestBenchmarkNameLength + 1}s %9.3f ± %4.1f%% (%8.3f) %s - %.1fx".format(
                    item.benchmark,
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

fun printCombinedResultsWithScaling(
    groupedAndSortedAndRenamed: Map<Map<String, String>, List<JmhResult>>,
    categoryMultipliers: Map<Map<String, String>, Double>
) {
    val benchmarkResults = groupedAndSortedAndRenamed.values.flatten()
        .groupBy(
            { it.benchmark },
            { result -> categoryMultipliers.getOrElse(result.params) { throw Exception("bad category") } * result.primaryMetric.score })
    val sortedTotalResults =
        benchmarkResults.map { (key, results) -> key to results.sum() }.sortedBy { (_, total) -> total }
    println("*** Totals, scaled to match")
    val longestBenchmarkNameLength = sortedTotalResults.max { (key, _) -> key.length } ?: 0
    val (_, firstScore) = sortedTotalResults.first()
    sortedTotalResults.forEach { (key, score) ->
        println("%-${longestBenchmarkNameLength + 1}s %9.3f - %.1fx".format(key, score, score / firstScore))
    }
}

private fun calculateCategoryMultipliers(groupedAndSortedAndRenamed: Map<Map<String, String>, List<JmhResult>>): Map<Map<String, String>, Double> {
    val fastestInCategories =
        groupedAndSortedAndRenamed.map { (key, results) -> key to results.first().primaryMetric.score }
    val slowestOverall =
        fastestInCategories.maxOfOrNull { (_, score) -> score } ?: throw Exception("no max")
    val categoryMultipliers = fastestInCategories.associate { (key, score) ->
        key to (slowestOverall / score)
    }
    return categoryMultipliers
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
