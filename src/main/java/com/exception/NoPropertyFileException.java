package com.exception;

public class NoPropertyFileException extends RuntimeException {
    public NoPropertyFileException(String message) {
        super(message);
    }
}