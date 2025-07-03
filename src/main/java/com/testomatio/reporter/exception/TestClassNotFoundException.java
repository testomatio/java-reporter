package com.testomatio.reporter.exception;

public class TestClassNotFoundException extends RuntimeException {
    public TestClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
