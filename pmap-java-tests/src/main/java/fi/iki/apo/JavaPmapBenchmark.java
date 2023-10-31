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

package fi.iki.apo;

import fi.iki.apo.pmap.JavaMapAlternatives.JavaMapFn;
import fi.iki.apo.pmap.LoadGenerator;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Warmup(iterations = 16)
@Measurement(iterations = 16)
@BenchmarkMode(Mode.AverageTime)
@Timeout(time = 5, timeUnit = TimeUnit.MINUTES)
@Fork(value = 1, warmups = 0)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class JavaPmapBenchmark {

    public static final String FAST = "fast";
    public static final String SLOW = "slow";
    @Param({FAST, SLOW})
    private String loadGeneratorType;

    private List<Integer> items;

    @Setup(Level.Invocation)
    public void setup() {
        items = LoadGenerator.listOfInts(1000000);
    }

    private Function<Integer, Integer> resolveLoadGenerator() {
        switch (loadGeneratorType) {
            case FAST:
                return LoadGenerator::looperFast;
            case SLOW:
                return LoadGenerator::looperSlow;
        }
        throw new RuntimeException("Unsupported LoadGenerator type " + loadGeneratorType);
    }

    @Benchmark
    public List<Integer> testJavaMapFor() {
        return JavaMapFn.mapFor(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaMapStream() {
        return JavaMapFn.mapStream(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapParallelStream() {
        return JavaMapFn.pmapParallelStream(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPool() {
        return JavaMapFn.pmapFixedThreadPool(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPoolDoubleThreads() {
        return JavaMapFn.pmapFixedThreadPoolDoubleThreads(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapFixedVirtualThreadPool() {
        return JavaMapFn.pmapFixedVirtualThreadPool(items, resolveLoadGenerator());
    }

    @Benchmark
    public List<Integer> testJavaPmapFixedVirtualThreadPoolDoubleThreads() {
        return JavaMapFn.pmapFixedVirtualThreadPoolDoubleThreads(items, resolveLoadGenerator());
    }
    @Benchmark
    public List<Integer> testJavaPmapVirtualThread() {
        return JavaMapFn.pmapNewVirtualThread(items, resolveLoadGenerator());
    }

    @Benchmark
    public List<Integer> testJavaPmapFixedThreadPoolFastCreateResolve() {
        return JavaMapFn.pmapFixedThreadPoolFastCreateResolve(items, resolveLoadGenerator());
    }
}
