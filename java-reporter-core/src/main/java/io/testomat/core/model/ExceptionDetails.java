package io.testomat.core.model;

/**
 * Container for exception details extracted from test failures.
 * Holds message and stack trace information for reporting.
 */
public class ExceptionDetails {
    private final String message;
    private final String stack;

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