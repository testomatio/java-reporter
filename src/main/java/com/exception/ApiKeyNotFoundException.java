package com.exception;

public class ApiKeyNotFoundException extends RuntimeException {
    public ApiKeyNotFoundException(String message) {
        super(message);
    }

    public ApiKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}