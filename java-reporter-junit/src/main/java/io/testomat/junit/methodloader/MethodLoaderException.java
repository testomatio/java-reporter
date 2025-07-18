package io.testomat.junit.methodloader;

public class MethodLoaderException extends RuntimeException {
    public MethodLoaderException(String message) {
        super(message);
    }

    public MethodLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
