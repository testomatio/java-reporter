package com.exception;

public class TestomatReportingException extends RuntimeException {
    public TestomatReportingException(String message) {
        super(message);
    }
    
    public TestomatReportingException(String message, Throwable cause) {
        super(message, cause);
    }
}