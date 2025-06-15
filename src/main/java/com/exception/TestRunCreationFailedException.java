package com.exception;

public class TestRunCreationFailedException extends RuntimeException {
    public TestRunCreationFailedException(String message) {
        super(message);
    }
}
