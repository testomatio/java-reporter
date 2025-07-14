package io.testomat.core.exception;

public class RequestExecutionFailedException extends RuntimeException {
    public RequestExecutionFailedException(String message, Throwable cause) {
        super(message);
    }

    public RequestExecutionFailedException(String message) {
        super(message);
    }
}
