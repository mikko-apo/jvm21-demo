package fi.iki.apo.pmap

import kotlin.math.*

object KotlinLoadGenerator {

    fun looperFast(i: Int): Int {
        var c = 0
        var counter = 0
        while (c < i) {
            counter += 1
            c += 1
        }
        return counter
    }

    fun looperSlow(i: Int): Int {
        var c = 0
        var counter: Long = 0
        while (c < i) {
            counter += 1
            c += 1
        }
        return counter.toInt()
    }

    fun powSqrt(i: Int): Int {
        return (sqrt((i + 1).toDouble()) + i).pow(2.0).toInt()
    }

    fun listOfInts(size: Int) = List(size) { i -> i }
}

