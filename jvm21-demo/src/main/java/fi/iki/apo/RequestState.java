package fi.iki.apo;

import fi.iki.apo.util.Benchmark;
import fi.iki.apo.util.StringHelpers;

import java.util.ArrayList;

import static fi.iki.apo.util.StringHelpers.joinStrings;
import static fi.iki.apo.util.StringHelpers.joinStringsWithDelimiter;

public class RequestState {
    public final long id;
    ForkState forkState = ForkState.NotProcessed;
    public boolean interruptedSend = false;
    public Exception sendException = null;
    public Integer badStatusCode = null;
    public String badContent = null;
    public boolean ok = false;
    public Throwable unhandledError;
    public Benchmark.Duration duration = null;

    public enum ForkState {NotProcessed, Forking, InterruptedBeforeFork, Forked}

    public RequestState(int id) {
        this.id = id;
    }

    public String getErrorDescription() {
        var errors = new ArrayList<String>();
        if (ok) return null;
        if (badContent != null || badStatusCode != null)
            errors.add("bad statuscode(=" + badStatusCode + ") or content : " + badContent);
        if (sendException != null)
            errors.add("caught exception when processing send(): " + StringHelpers.resolveErrorDescription(sendException));
        if (interruptedSend) errors.add("interrupted send()");
        if (unhandledError != null) {
            errors.add("caught unhandled exception" + StringHelpers.resolveErrorDescription(unhandledError));
        }
        if (errors.isEmpty()) {
            errors.add("not processed");
        }
        errors.add(joinStrings("forkState:", forkState));
        return joinStringsWithDelimiter(", ", errors.reversed().toArray());
    }
}
