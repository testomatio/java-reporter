package io.testomat.junit.exception;

/**
 * Base exception for method export-related failures.
 * This exception is thrown when test method body extraction, parsing,
 * or transmission to the Testomat.io API encounters errors.
 */
public class MethodExporterException extends RuntimeException {
    public MethodExporterException(String message) {
        super(message);
    }

    public MethodExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
