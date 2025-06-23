package com.testomatio.reporter.core.constructor;

import lombok.Getter;

@Getter
public final class ExceptionDetails {
    private final String message;
    private final String stack;

    ExceptionDetails(String message, String stack) {
        this.message = message;
        this.stack = stack;
    }

    static ExceptionDetails empty() {
        return new ExceptionDetails(null, null);
    }
}