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
map for(T t : list) 16 repeats: 75% percentile 33ms [ 12ms - 53ms ] average 27ms std dev 32,25% 8ms
pmap list.parallelStream() 16 repeats: 75% percentile 54ms [ 28ms - 78ms ] average 48ms std dev 18,74% 9ms
map list.stream() 16 repeats: 75% percentile 62ms [ 38ms - 73ms ] average 58ms std dev 12,86% 7ms
pmap newFixedThreadPool 16 repeats: 75% percentile 229ms [ 156ms - 246ms ] average 214ms std dev 10,44% 22ms
pmap fixedVirtualThreads 16 repeats: 75% percentile 273ms [ 174ms - 299ms ] average 240ms std dev 16,04% 38ms
pmap newVirtualThreadPerTaskExecutor 16 repeats: 75% percentile 683ms [ 508ms - 706ms ] average 646ms std dev 7,15% 46ms
```

Kotlin:

```
map List(n){} 16 repeats: 75% percentile 32ms [ 12ms - 56ms ] average 26ms std dev 35,91% 9ms
map for(t : list) 16 repeats: 75% percentile 28ms [ 23ms - 51ms ] average 28ms std dev 19,69% 5ms
map list.map{} 16 repeats: 75% percentile 36ms [ 16ms - 53ms ] average 32ms std dev 23,54% 7ms
pmap newFixedThreadPool 16 repeats: 75% percentile 222ms [ 151ms - 240ms ] average 211ms std dev 9,65% 20ms
pmap fixedVirtualThreads 16 repeats: 75% percentile 254ms [ 192ms - 276ms ] average 239ms std dev 9,75% 23ms
pmap newVirtualThreadPerTaskExecutor 16 repeats: 75% percentile 646ms [ 494ms - 744ms ] average 607ms std dev 10,95% 66ms
pmap Coroutines 16 repeats: 75% percentile 1,434s [ 1,378s - 1,441s ] average 1,420s std dev 1,24% 17ms
pmap Coroutines mapAsync semaphore 16 repeats: 75% percentile 1,505s [ 1,371s - 1,573s ] average 1,481s std dev 3,63% 53ms
pmap Coroutines mapAsync 16 repeats: 75% percentile 1,506s [ 1,379s - 1,550s ] average 1,493s std dev 2,17% 32ms
pmap pmapThreadPoolCoroutines 16 repeats: 75% percentile 1,508s [ 1,389s - 1,619s ] average 1,499s std dev 2,41% 36ms
```

Observations:

* Java's for(T t : list) and Kotlin's List(n){}, for(t : list) and list.map{} are the fastest and on the same level
* Java's list.parallelStream() has the best parallel performance, but is still 2x slower
* Java's list.stream() has similar overhead (at least 2x)
* Using pooled threads has significant overhead (at least 7x)
* newVirtualThreadPerTaskExecutor has even worse overhead (at least 21x)
* Coroutines are unfortunately in a different league (44x)

Analysis:

Using a task with very low CPU requirements doesn't show the advantages in parallel processing, instead there's
overhead.

### Round 2 - Slow tests

Java:

```
pmap fixedVirtualThreads 8 repeats: 75% percentile 2,353s [ 2,192s - 2,546s ] average 2,328s std dev 3,44% 80ms
pmap newFixedThreadPool 8 repeats: 75% percentile 2,492s [ 2,350s - 2,519s ] average 2,435s std dev 2,53% 61ms
pmap newVirtualThreadPerTaskExecutor 8 repeats: 75% percentile 2,576s [ 2,498s - 2,851s ] average 2,580s std dev 3,36% 86ms
map for(T t : list) 8 repeats: 75% percentile 10,212s [ 10,184s - 10,228s ] average 10,209s std dev 0,10% 10ms
pmap list.parallelStream() 8 repeats: 75% percentile 12,391s [ 12,253s - 12,434s ] average 12,361s std dev 0,42% 51ms
map list.stream() 8 repeats: 75% percentile 20,396s [ 20,361s - 20,548s ] average 20,394s std dev 0,24% 48ms
```

Kotlin:

```
pmap fixedVirtualThreads 4 repeats: 75% percentile 2,432s [ 2,338s - 2,484s ] average 2,403s std dev 2,25% 54ms
pmap newFixedThreadPool 4 repeats: 75% percentile 2,560s [ 2,447s - 2,716s ] average 2,540s std dev 4,08% 103ms
pmap newVirtualThreadPerTaskExecutor 4 repeats: 75% percentile 2,667s [ 2,433s - 2,775s ] average 2,602s std dev 4,73% 123ms
map List(n){} 4 repeats: 75% percentile 10,227s [ 10,177s - 10,227s ] average 10,206s std dev 0,21% 21ms
map list.map{} 4 repeats: 75% percentile 10,221s [ 10,210s - 10,230s ] average 10,217s std dev 0,08% 8ms
map for(t : list) 4 repeats: 75% percentile 10,231s [ 10,219s - 10,238s ] average 10,228s std dev 0,07% 6ms
pmap Coroutines 4 repeats: 75% percentile 11,599s [ 11,500s - 11,666s ] average 11,564s std dev 0,57% 65ms
pmap Coroutines mapAsync semaphore 4 repeats: 75% percentile 11,649s [ 11,556s - 11,708s ] average 11,620s std dev 0,49% 56ms
pmap pmapThreadPoolCoroutines 4 repeats: 75% percentile 11,672s [ 11,564s - 11,683s ] average 11,633s std dev 0,40% 46ms
pmap Coroutines mapAsync 4 repeats: 75% percentile 11,710s [ 11,608s - 11,740s ] average 11,668s std dev 0,46% 54ms
```

Observations:

* Increasing task's cpu load helps in showing that parallel processing can increase performance
* Fixed thread pools have best performance. Virtual threads might have less overhead.
* newVirtualThreadPerTaskExecutor has managed to almost reach the pooled alternatives in performance
* Java's parallelStream() is not able to run the tasks in parallel. That might explain it's good performance with low
  cpu tasks
* Java's list.stream() has significant overhead (10s vs 20s) vs Java's for(T t : list)
* Coroutines alternatives have similar performance and are almost 5x slower than top

Analysis:

For light cpu tasks, pmap() does not help even with one million items. For high cpu tasks, virtual thread pooled map()
has best performance.