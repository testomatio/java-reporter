package io.testomat.junit.exception;

public class ReporterException extends RuntimeException {
    public ReporterException(String message, Throwable e) {
        super(message, e);
    }
}
