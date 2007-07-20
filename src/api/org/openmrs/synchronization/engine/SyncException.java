package org.openmrs.synchronization.engine;

public class SyncException extends RuntimeException {

    public static final long serialVersionUID = 0L;
        

    public SyncException() {
    }

    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
