package com.testomatio.reporter.exception;

public class RequestStatusNotSuccessException extends RuntimeException {
    public RequestStatusNotSuccessException(String message) {
        super(message);
    }
}
