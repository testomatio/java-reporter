package com.testomatio.reporter.exception;

import java.net.URISyntaxException;

public class RequestUriBuildingException extends RuntimeException {
    public RequestUriBuildingException(String message, URISyntaxException e) {
        super(message);
    }
}
