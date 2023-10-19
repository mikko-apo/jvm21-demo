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
compare. I wrote 6 implementations for Java and 5 for Kotlin.

## Avoiding microbenchmark issues

I didn't manage to configure JMH to provide sensible data with the long running tests, so there might be
microbenchmarking related issues. I tried take handle those with following approaches:

* tests are using one million items and each approach is tested 8 items
* data is accessed from an ArrayList of one million items. This increases CPU cache misses
* there are two load functions that use CPU. One is added to a counter n times based on the item's sequence number. Fast
  versions uses int counter, Slow versions use long counter.
* Functions are warmed up with /10 items (100000) and there are one second sleep between each map() tested function
    * load functions are warmed up first
    * each tested function is warmed up also and tested 8 times
* Functions return values and use return values to ensure that JIT does not optimize the function out
* Functions are tested with one million items, 8 times repeatedly with one second sleep between tested functions

## Results

### Round 1 - Fast tests

Java:
```
0ms - Warming load function
10ms - Warming tested functions with 100000 items
7,037s - Warming up done. Sleeping 1 second.
8,043s - Testing with 1000000 items
----------------------
55ms - map with Java for(T t : list) average duration per 8 repeats 6ms - 
331ms - map with Java list.stream() average duration per 8 repeats 41ms - 
423ms - pmap with Java list.parallelStream() average duration per 8 repeats 52ms - 
1,979s - pmap with Java fixedVirtualThreads average duration per 8 repeats 247ms - 
1,290s - pmap with Java newFixedThreadPool average duration per 8 repeats 161ms - 
5,682s - pmap with Java newVirtualThreadPerTaskExecutor average duration per 8 repeats 710ms - 
```

Kotlin:
```
0ms - Warming load function
20ms - Warming tested functions with 100000 items
5,785s - Warming up done. Testing with 1000000 items
----------------------
100ms - map with Kotlin list.map{} average duration per 8 repeats 12ms - 
1,380s - pmap with Kotlin fixedVirtualThreads average duration per 8 repeats 172ms - 
1,203s - pmap with Kotlin newFixedThreadPool average duration per 8 repeats 150ms - 
4,668s - pmap with Kotlin newVirtualThreadPerTaskExecutor average duration per 8 repeats 583ms - 
11,728s - pmap with Kotlin Coroutines average duration per 8 repeats 1,466s - 
```

Observations:

* Kotlin runs the warm up faster
* Java's for(T t : list) is the fastest, then Kotlin's list.map{}
* Java's list.parallelStream() has the best parallel performance, but is still 7x slower
* Java's list.stream() has pretty high overhead (at least 6x)
* Using pooled threads has significant overhead (at least 21x)
* newVirtualThreadPerTaskExecutor has even worse overhead (at least 84x)
* Coroutines are unfortunately in a different league (213x)
* Parallel processing has high overhead with tasks that require

Using a task with very low CPU requirements doesn't show the advantages in parallel processing, instead there's
overhead.

Analysis:

With tasks that have low cpu usage, parallel processing is 

### Round 2 - Slow tests

Java:
```
0ms - Warming load function
131ms - Warming tested functions with 100000 items
11,607s - Warming up done. Sleeping 1 second.
12,630s - Testing with 1000000 items
----------------------
82,365s - map with Java for(T t : list) average duration per 8 repeats 10,295s - 
165,090s - map with Java list.stream() average duration per 8 repeats 20,636s - 
102,699s - pmap with Java list.parallelStream() average duration per 8 repeats 12,837s - 
20,557s - pmap with Java fixedVirtualThreads average duration per 8 repeats 2,569s - 
21,230s - pmap with Java newFixedThreadPool average duration per 8 repeats 2,653s - 
24,387s - pmap with Java newVirtualThreadPerTaskExecutor average duration per 8 repeats 3,048s - 
```

Kotlin:
```
0ms - Warming load function
No 135ms - Warming tested functions with 100000 items
8,701s - Warming up done. Testing with 1000000 items
----------------------
82,852s - map with Kotlin list.map{} average duration per 8 repeats 10,356s - 
18,094s - pmap with Kotlin fixedVirtualThreads average duration per 8 repeats 2,261s - 
18,788s - pmap with Kotlin newFixedThreadPool average duration per 8 repeats 2,348s - 
20,996s - pmap with Kotlin newVirtualThreadPerTaskExecutor average duration per 8 repeats 2,624s - 
93,549s - pmap with Kotlin Coroutines average duration per 8 repeats 11,693s - 
```
