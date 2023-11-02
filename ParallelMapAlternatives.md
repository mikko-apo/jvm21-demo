# Parallel Map Alternatives with Java and Kotlin

With Project Loom, the subjects of parallelism and concurrency became relevant topic in the JVM ecosystem.
Project Loom provides asynchronous performance with regular synchronous thread-per-request Java code.

## Simple example of how Project Loom's Virtual threads work

If you run the following function in regular OS thread, the OS thread will be blocked when the code calls read() for the
socket.
If it is run in a Virtual Thread, the JVM unmounts the execution context (continuation) from the OS carrier thread. When
the data is ready and there is a free OS thread, the continuation will be mounted to some OS thread and execution will
continue with the data.

```
public String getUrlContents(String url)  {
    try(InputStream in  = URI.create(url).toURL().openStream()) {
        byte[] bytes = in.readAllBytes(); // Socket read() blocks OS Thread!!
        return new String(bytes);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```

The great thing about Project Loom is that the application code doesn't need to be changed to get the concurrency
performance advantages. The programming paradigm stays the same. The feature works for all JVM based languages.
Async programming without the hassle of async/await or promises or futures or callbacks.

I think Project Loom is a great improvement for the JVM ecosystem and together with Kotlin, GraalVM, new microservice
based server frameworks and other performance improvements JVM is becoming a serious contender for backend development.

# Understanding the performance and limitations of various approaches

I wrote test code that tests various map and parallel map implementations using adjustable load to understand how they
compare. I wrote 6 implementations for Java and 10 for Kotlin.

## Avoiding microbenchmark issues

I didn't manage to configure JMH to provide sensible data with the long running tests, so there might be
microbenchmarking related issues. I tried take handle those with following approaches:

* tests are using one million items and each approach is tested 8 items
* data is accessed from an ArrayList of one million items. This increases CPU cache misses. The array is regenerated for
  each repeat
* there are two load functions that use CPU. One is added to a counter n times based on the item's sequence number. Fast
  versions uses int counter, Slow versions use long counter.
* Functions are warmed up with /10 items (100000) and there are one second sleep between each map() tested function
    * load functions are warmed up first
    * each tested function is warmed up also and tested 8 times
* Functions return values and use return values to ensure that JIT does not optimize the function out
* Functions are tested with one million items, 8 times repeatedly with one second sleep between tested functions
* Test code is implemented fully in Java and fully in Kotlin. This ensures there are no issues switching between Java
  and Kotlin

## Results

### Round 1 - Fast tests

```
Java.pmapParallelStream                           9,305 ± 10,1% (   0,941) ms/op - 1,0x
Kotlin.pmapKotlinParallelStreamMap                9,728 ± 10,9% (   1,056) ms/op - 1,0x
Kotlin.pmapJavaParallelStreamMap                 10,149 ±  7,9% (   0,807) ms/op - 1,1x
Java.mapFor                                      12,484 ±  8,1% (   1,015) ms/op - 1,3x
Kotlin.mapListConstructor                        12,571 ±  8,3% (   1,047) ms/op - 1,4x
Kotlin.mapWithoutThreads                         12,860 ±  7,6% (   0,971) ms/op - 1,4x
Java.mapStream                                   13,045 ±  5,8% (   0,751) ms/op - 1,4x
Kotlin.mapFor                                    21,045 ± 10,5% (   2,220) ms/op - 2,3x
Java.pmapFixedThreadPool                        161,611 ±  2,7% (   4,361) ms/op - 17,4x
Java.pmapFixedThreadPoolDoubleThreads           163,592 ±  2,8% (   4,567) ms/op - 17,6x
Java.pmapFixedThreadPoolFastCreateResolve       164,683 ±  1,8% (   3,013) ms/op - 17,7x
Kotlin.pmapFixedThreadPool                      181,376 ±  1,6% (   2,816) ms/op - 19,5x
Java.pmapFixedReusedVirtualThreadPool           192,163 ±  2,6% (   5,076) ms/op - 20,7x
Java.pmapFixedVirtualThreadPool                 196,629 ±  2,5% (   4,926) ms/op - 21,1x
Java.pmapFixedVirtualThreadPoolDoubleThreads    197,797 ±  2,5% (   5,008) ms/op - 21,3x
Kotlin.pmapFixedVirtualThreadPool               208,543 ±  2,8% (   5,940) ms/op - 22,4x
Java.pmapVirtualThread                          299,377 ±  3,0% (   9,124) ms/op - 32,2x
Kotlin.pmapCoroutines                           300,602 ±  3,0% (   9,151) ms/op - 32,3x
Kotlin.pmapNewVirtualThread                     324,330 ±  2,6% (   8,355) ms/op - 34,9x
Kotlin.pmapCoroutinesCC                         419,869 ±  3,2% (  13,404) ms/op - 45,1x
Kotlin.pmapCoroutinesCCSemaphore                448,504 ±  1,9% (   8,718) ms/op - 48,2x
Kotlin.pmapCoroutinesThreadPool                 455,376 ±  5,0% (  22,875) ms/op - 48,9x
```

Smaller is better, values with comma as decimal separator

Observations:

* Java's list.parallelStream() has the best performance. When called from Kotlin has some overhead.
* Java's for(T t : list) and Kotlin's Kotlin's List(n){} and list.map{} and Java's list.stream() have roughly the same
  performance
* Using pooled threads has significant overhead (at least 17x)
* newVirtualThreadPerTaskExecutor has even worse overhead (at 32x)
* Coroutines are unfortunately in a different league (at 32x-48x)

Analysis:

* Java's parallelStream() is able to show performance improvements even for tasks with light cpu requirements.
* Other approaches suffer from various things that cause overhead: unnecessary list creation, thread and thread pool
  overhead and coroutine overhead
* parallelStream() is probably able to improve performance because it streams items and does not create big temporary
  lists. Also, it's able to reuse its thread pool when other approaches recreate pools.

### Round 2 - Slow tests

```
Java.pmapParallelStream                        2067,098 ±  5,2% ( 107,378) ms/op - 1,0x
Kotlin.pmapJavaParallelStreamMap               2189,099 ±  1,3% (  27,738) ms/op - 1,1x
Java.pmapFixedVirtualThreadPoolDoubleThreads   2190,051 ±  1,7% (  37,316) ms/op - 1,1x
Java.pmapFixedThreadPoolDoubleThreads          2207,862 ±  1,3% (  28,740) ms/op - 1,1x
Kotlin.pmapKotlinParallelStreamMap             2208,368 ±  2,4% (  53,114) ms/op - 1,1x
Java.pmapFixedVirtualThreadPool                2212,728 ±  2,3% (  51,478) ms/op - 1,1x
Java.pmapVirtualThread                         2248,001 ±  1,6% (  36,750) ms/op - 1,1x
Java.pmapFixedThreadPool                       2281,373 ±  1,6% (  36,753) ms/op - 1,1x
Java.pmapFixedThreadPoolFastCreateResolve      2313,085 ±  2,4% (  56,565) ms/op - 1,1x
Kotlin.pmapNewVirtualThread                    2373,451 ±  1,2% (  28,602) ms/op - 1,1x
Kotlin.pmapFixedVirtualThreadPool              2375,092 ±  1,2% (  29,038) ms/op - 1,1x
Kotlin.pmapFixedThreadPool                     2389,740 ±  1,0% (  24,613) ms/op - 1,2x
Java.pmapFixedReusedVirtualThreadPool          2397,850 ±  2,3% (  54,308) ms/op - 1,2x
Kotlin.pmapCoroutinesThreadPool                2920,393 ±  2,4% (  69,910) ms/op - 1,4x
Kotlin.mapListConstructor                     10118,209 ±  0,2% (  15,329) ms/op - 4,9x
Kotlin.mapWithoutThreads                      10122,590 ±  0,1% (  11,524) ms/op - 4,9x
Java.mapFor                                   10128,835 ±  0,2% (  24,039) ms/op - 4,9x
Java.mapStream                                10134,930 ±  0,3% (  32,327) ms/op - 4,9x
Kotlin.mapFor                                 10142,976 ±  0,2% (  19,113) ms/op - 4,9x
Kotlin.pmapCoroutines                         10467,096 ±  1,0% ( 106,503) ms/op - 5,1x
Kotlin.pmapCoroutinesCCSemaphore              10510,765 ±  0,8% (  88,238) ms/op - 5,1x
Kotlin.pmapCoroutinesCC                       10551,250 ±  1,1% ( 112,887) ms/op - 5,1x
```

Smaller is better, values with comma as decimal separator

Observations:

* Increasing task's cpu load helps in showing that parallel processing can increase performance
* Java's list.parallelStream() has still the best performance. When called from Kotlin has some overhead.
* Fixed thread pool based approaches caught up with parallelStream()
* Doubling thread count in pools helps just a bit
* Fixed virtual thread pools might have a bit less overhead.
* newVirtualThreadPerTaskExecutor has managed to almost reach the pooled alternatives in performance
* Kotlin's coroutines need to use fixed thread pool as the dispatcher to achieve good parallel performance. The default
  dispatcher is recommended by Kotlin documentation for CPU based tasks, but the test show it doesn't work for some
  reason.
* Coroutines alternatives (without a configured thread pool) have the worst performance and are almost 5x slower than
  top

Analysis:

* Java's parallelStream() is the best option for parallel map(), for any cpu based load, whether the task is light or
  heavy. There might be some overhead in calling it from for example Kotlin, but the difference is small (4-6%) and
  it still beats the other alternatives.
* Hand written parallel map alternatives can have worse performance characteristics due multiple reasons, even if
  they're suggested in various internet posts and forums.
* Kotlin's coroutines seem to have bad default for dispatcher and even with comparable thread pool the
  overhead is from fairly big to significant (1.4x to 32x).
* Virtual threads work with fixed thread pools for heavy cpu based tasks without significant overhead, but
  parallelStream() has better performance with CPU based tasks.
* parallelStream()'s interaction with virtual threads is not documented.

TODO:
* parallelStream with virtual threads
* test .collect() .toList() on where it's run: virtual thread or OS thread
