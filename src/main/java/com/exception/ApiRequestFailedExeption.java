package com.exception;

public class ApiRequestFailedExeption extends RuntimeException {
    public ApiRequestFailedExeption(String message) {
        super(message);
    }
}
