package io.testomat.core.model;

/**
 * Container for exception details extracted from test failures.
 * Holds error message and stack trace information for diagnostic reporting to Testomat.io.
 * Immutable data structure for safe sharing across reporting components.
 */
public class ExceptionDetails {
    private final String message;
    private final String stack;

    /**
     * Creates exception details with message and stack trace.
     * 
     * @param message error or exception message, may be null
     * @param stack full stack trace string, may be null
     */
    public ExceptionDetails(String message, String stack) {
        this.message = message;
        this.stack = stack;
    }

    /**
     * Creates empty exception details for tests without exceptions.
     *
     * @return ExceptionDetails with null message and stack
     */
    public static ExceptionDetails empty() {
        return new ExceptionDetails(null, null);
    }

    public String getMessage() {
        return message;
    }

    public String getStack() {
        return stack;
    }
}