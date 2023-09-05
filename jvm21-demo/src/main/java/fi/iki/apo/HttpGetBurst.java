package fi.iki.apo;

import fi.iki.apo.util.Benchmark;
import fi.iki.apo.util.HandledRuntimeException;
import fi.iki.apo.util.StringHelpers;
import fi.iki.apo.util.Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

import static fi.iki.apo.util.Benchmark.formatDuration;
import static fi.iki.apo.util.CollectionHelpers.groupBy;
import static fi.iki.apo.util.CollectionHelpers.split;
import static fi.iki.apo.util.StringHelpers.joinStrings;

public class HttpGetBurst {

    public static void main(String[] args) {
        new HttpGetBurst().runBursts(
                "http://localhost:3000",
                130,
                "Hello World!",
                100
        );
    }

    void runBursts(String url, int reqCount, String expectedContent, int repeatCount) {
        for (int i = 0; i < repeatCount; i++) {
            System.out.println(joinStrings("\n*** Running burst", i + 1));
            runBurst(url, reqCount, expectedContent);
            Utils.sleep(Duration.ofSeconds(1));
        }
    }

    void runBurst(String url, int reqCount, String expectedContent) {
        final var dest = URI.create(url);
        final var benchmark = new Benchmark();
        final var requestList = new ArrayList<RequestState>(reqCount);
        for (int i = 0; i < reqCount; i++) {
            requestList.add(new RequestState(i + 1));
        }
        // initialize and automatically close virtualThreadExecutor, httpClient and StructuredTaskScope
        try (final var virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            try (final var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).executor(virtualThreadExecutor).build()) {
                try (final var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    launchAndProcessRequests(scope, httpClient, requestList, dest, expectedContent, benchmark);
                } finally {
                    // scope is closed in finally block, it's safe to process requestList
                    printResults(requestList);
                }
            }
        }
    }

    private static void launchAndProcessRequests(
            StructuredTaskScope.ShutdownOnFailure scope,
            HttpClient httpClient,
            ArrayList<RequestState> requestList,
            URI dest,
            String expectedContent,
            Benchmark benchmark
    ) {
        // launch requests from a forked virtual thread. if any request fails, this thread should stop sending requests
        scope.fork(() -> {
            requestList.forEach(requestState -> {
                // Check if a request in scope has thrown an error. Scope should interrupt virtual launch thread.
                // I'm not sure if this works.
                if (Thread.currentThread().isInterrupted()) {
                    requestState.forkState = RequestState.ForkState.InterruptedBeforeFork;
                    requestState.duration = benchmark.getDuration();
                } else {
                    requestState.forkState = RequestState.ForkState.Forking;
                    requestState.duration = benchmark.getDuration();
                    // fork each request processing to its own virtual thread
                    scope.fork(() -> {
                        requestState.forkState = RequestState.ForkState.Forked;
                        try {
                            requestState.duration = benchmark.getDuration();
                            return getUrlAndAssertContent(httpClient, dest, expectedContent, requestState, benchmark);
                        } catch (HandledRuntimeException e) {
                            throw e;
                        } catch (Throwable t) {
                            // try to catch unhandled Exceptions
                            requestState.unhandledError = t;
                            requestState.duration = benchmark.getDuration();
                            t.printStackTrace();
                            throw t;
                        }
                    });
                    if (requestState.id % 1000 == 0) {
                        benchmark.print("Forked request counter", requestState.id);
                    }
                }
            });
            benchmark.print("Launched", requestList.size(), "requests in total");
            return "";
        });
        // wait until all forked virtual threads are finished or one of them fails
        try {
            scope.join();
        } catch (InterruptedException e) {
            final var message = joinStrings("scope.join() was interrupted", StringHelpers.resolveErrorDescription(e));
            benchmark.print(message);
            throw new RuntimeException(message, e);
        }
        // throw exception if a subtask failed
        try {
            scope.throwIfFailed();
        } catch (ExecutionException e) {
            final var message = joinStrings("something threw an exception in StructuredTaskScope", StringHelpers.resolveErrorDescription(e));
            benchmark.print(message);
            throw new RuntimeException(message, e);
        }
        benchmark.print(requestList.size(), "requests completed without errors");
    }

    private static String getUrlAndAssertContent(HttpClient httpClient, URI url, String expectedContent, RequestState requestState, Benchmark benchmark) {
        final var response = executeRequest(httpClient, url, requestState, benchmark);
        String res = response.body();
        if (response.statusCode() != 200 || !expectedContent.equals(res)) {
            requestState.badStatusCode = response.statusCode();
            requestState.badContent = res;
            requestState.duration = benchmark.getDuration();
            throw new HandledRuntimeException(joinStrings("request", requestState.id, requestState.getErrorDescription()));
        }
        requestState.ok = true;
        requestState.duration = benchmark.getDuration();
        return res;
    }

    private static HttpResponse<String> executeRequest(HttpClient httpClient, URI url, RequestState requestState, Benchmark benchmark) {
        try {
            var request = HttpRequest.newBuilder(url)
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            requestState.interruptedSend = true;
            requestState.duration = benchmark.getDuration();
            throw new HandledRuntimeException(e);
        } catch (Exception e) {
            requestState.sendError = e;
            requestState.duration = benchmark.getDuration();
            throw new HandledRuntimeException(e);
        }
    }

    private void printResults(ArrayList<RequestState> requestList) {
        final var sortedRequests = split(requestList, requestState -> requestState.ok);
        List<RequestState> good = sortedRequests.good;
        if (!good.isEmpty()) {
            System.out.println(joinStrings(good.size(), "requests succeeded"));
            final var reqDurationSum = good.stream().mapToDouble(req -> req.duration.durationMs).sum();
            long averageReqDurationMs = (long) (reqDurationSum / good.size());
            System.out.println(joinStrings("-", "average request duration", formatDuration(averageReqDurationMs)));
        }
        if (!sortedRequests.bad.isEmpty()) {
            System.out.println(joinStrings(sortedRequests.bad.size(), "requests failed"));
            final var groupedFailed = groupBy(sortedRequests.bad, RequestState::getErrorDescription);
            final var sortedFailedKeys = new ArrayList<>(groupedFailed.keySet());
            sortedFailedKeys.sort(Comparator.comparingInt(k -> groupedFailed.get(k).size()));
            for (String key : sortedFailedKeys) {
                var failed = groupedFailed.get(key);
                System.out.println(joinStrings("-", key, failed.size()));
            }
        }
    }

}
