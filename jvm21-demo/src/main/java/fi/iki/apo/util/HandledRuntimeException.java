package fi.iki.apo.util;

public class HandledRuntimeException extends RuntimeException {

    public HandledRuntimeException(String message) {
        super(message);
    }

    public HandledRuntimeException(Exception e) {
        super(e);
    }
}
