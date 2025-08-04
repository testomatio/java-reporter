package io.testomat.core.exception;

public class TestClassNotFoundException extends RuntimeException {
    public TestClassNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
