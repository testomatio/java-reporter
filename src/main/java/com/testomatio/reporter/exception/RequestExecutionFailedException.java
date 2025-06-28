package com.testomatio.reporter.exception;

public class RequestExecutionFailedException extends RuntimeException {
    public RequestExecutionFailedException(String message, Throwable cause) {
        super(message);
    }
}
