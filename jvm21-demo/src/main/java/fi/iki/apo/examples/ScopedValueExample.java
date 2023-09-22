package fi.iki.apo.examples;

// simplified version of https://github.com/wesleyegberto/java-new-features/blob/master/java-21/ScopedValueUsageExample.java
public class ScopedValueExample {
    final static ScopedValue<String> MAIN_SCOPE = ScopedValue.newInstance();
    final static ScopedValue<String> WORKER_SCOPE = ScopedValue.newInstance();

    public static void main(String[] args) {
        // we can share a value from here
        ScopedValue.where(MAIN_SCOPE, "FOO").run(() -> {
            var worker = new Worker();
            worker.execute();
        });
    }

    static class Worker {
        public void execute() {
            log("start");
            ScopedValue.where(WORKER_SCOPE, "BAR").run(() -> {
                log("nested scope, main and worker defined");
            });
            ScopedValue.where(MAIN_SCOPE, "POW").run(() -> {
                log("redefined main");
            });
            log("original main");
        }
    }

    public static void log(String message) {
        System.out.println(message + " - main: " + MAIN_SCOPE.get() + ", worker: " + WORKER_SCOPE.orElse("NOT DEFINED"));
    }
}
