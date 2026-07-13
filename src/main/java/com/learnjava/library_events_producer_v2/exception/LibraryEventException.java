package com.learnjava.library_events_producer_v2.exception;

/**
 * Custom exception for library event operations.
 */
public class LibraryEventException extends RuntimeException {

    private String errorCode;

    public LibraryEventException(String message) {
        super(message);
        this.errorCode = "LIBRARY_EVENT_ERROR";
    }

    public LibraryEventException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public LibraryEventException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "LIBRARY_EVENT_ERROR";
    }

    public LibraryEventException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

