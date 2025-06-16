package com.testomatio.reporter.exception;

public class ApiRequestFailedException extends RuntimeException {
    public ApiRequestFailedException(String message) {
        super(message);
    }
}
