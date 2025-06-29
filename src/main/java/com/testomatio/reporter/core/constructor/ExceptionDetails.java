package com.testomatio.reporter.core.constructor;

import lombok.Getter;

/**
 * Immutable container for exception message and stack trace.
 */
@Getter
public final class ExceptionDetails {
    private final String message;
    private final String stack;

    ExceptionDetails(String message, String stack) {
        this.message = message;
        this.stack = stack;
    }

    /**
     * Creates empty exception details with null message and stack.
     */
    static ExceptionDetails empty() {
        return new ExceptionDetails(null, null);
    }
}
