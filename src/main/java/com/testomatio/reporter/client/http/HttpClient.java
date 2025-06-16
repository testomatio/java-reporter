package com.testomatio.reporter.client.http;

import java.io.IOException;

/**
 * Interface for HTTP client operations.
 * Provides abstraction for making HTTP requests and handling responses.
 */
public interface HttpClient {
    
    /**
     * Executes an HTTP POST request with JSON body.
     *
     * @param url the target URL
     * @param requestBody the request body as JSON string
     * @param responseType the expected response type class
     * @param <T> the response type
     * @return the deserialized response object, or null if no response expected
     * @throws IOException if the request fails or response cannot be processed
     */
    <T> T post(String url, String requestBody, Class<T> responseType) throws IOException;
    
    /**
     * Executes an HTTP PUT request with JSON body.
     *
     * @param url the target URL
     * @param requestBody the request body as JSON string
     * @param responseType the expected response type class
     * @param <T> the response type
     * @return the deserialized response object, or null if no response expected
     * @throws IOException if the request fails or response cannot be processed
     */
    <T> T put(String url, String requestBody, Class<T> responseType) throws IOException;
}