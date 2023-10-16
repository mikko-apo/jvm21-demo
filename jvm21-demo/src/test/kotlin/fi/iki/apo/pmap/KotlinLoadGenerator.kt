package fi.iki.apo.pmap


object KotlinLoadGenerator {
    fun looperSlowLongParameter(i: Long): Long {
        var c = 0
        var counter: Long = 0
        val endIndex = i.toInt()
        while (c < endIndex) {
            counter += 1
            c += 1
        }
        return counter
    }

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
}

