package io.testomat.core.exception;

public class RequestStatusNotSuccessException extends RuntimeException {
    public RequestStatusNotSuccessException(String message) {
        super(message);
    }
}
