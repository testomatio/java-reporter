package io.testomat.junit.model;

/**
 * Immutable container for exception message and stack trace.
 */
public final class ExceptionDetails {
    private final String message;
    private final String stack;

    public ExceptionDetails(String message, String stack) {
        this.message = message;
        this.stack = stack;
    }

    /**
     * Creates empty exception details with null message and stack.
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
