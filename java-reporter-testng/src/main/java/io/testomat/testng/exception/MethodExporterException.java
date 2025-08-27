package io.testomat.testng.exception;

/**
 * Exception thrown during TestNG method export operations.
 * Used when errors occur while processing, parsing, or sending test method data.
 */
public class MethodExporterException extends RuntimeException {
    
    /**
     * Constructs a new method exporter exception with the specified detail message.
     *
     * @param message the detail message describing the error
     */
    public MethodExporterException(String message) {
        super(message);
    }

    /**
     * Constructs a new method exporter exception with the specified detail message and cause.
     *
     * @param message the detail message describing the error
     * @param cause the cause of the exception
     */
    public MethodExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
