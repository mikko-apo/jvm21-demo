/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

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
