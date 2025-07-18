package io.testomat.junit.methodexporter;

public class MethodExporterException extends RuntimeException {
    public MethodExporterException(String message) {
        super(message);
    }

    public MethodExporterException(String message, Throwable cause) {
        super(message, cause);
    }
}
