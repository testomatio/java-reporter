package com.exception;

public class ReportingFailedException extends RuntimeException {
    public ReportingFailedException(String message) {
        super(message);
    }
}