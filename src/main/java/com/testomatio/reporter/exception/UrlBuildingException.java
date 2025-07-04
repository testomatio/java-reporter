package com.testomatio.reporter.exception;

public class UrlBuildingException extends RuntimeException {
    public UrlBuildingException(String message) {
        super(message);
    }

    public UrlBuildingException(String message, Throwable cause) {
        super(message);
    }
}
