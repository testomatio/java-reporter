package io.testomat.junit.exception;

/**
 * Exception thrown when test result reporting to Testomat.io fails.
 * This is a general-purpose exception for reporting-related failures.
 */
public class ReporterException extends RuntimeException {
    public ReporterException(String message, Throwable e) {
        super(message, e);
    }
}
