package com.testomatio.reporter.client.http;

import java.io.IOException;

/**
 * HTTP client abstraction for making API requests.
 */
public interface CustomHttpClient {

    /**
     * Executes HTTP POST request with JSON body.
     *
     * @param url target URL
     * @param requestBody JSON request body
     * @param responseType expected response type class
     * @param <T> response type
     * @return deserialized response object or null if no response expected
     * @throws IOException if request fails or response cannot be processed
     */
    <T> T post(String url, String requestBody, Class<T> responseType) throws IOException;

    /**
     * Executes HTTP PUT request with JSON body.
     *
     * @param url target URL
     * @param requestBody JSON request body
     * @param responseType expected response type class
     * @param <T> response type
     * @return deserialized response object or null if no response expected
     * @throws IOException if request fails or response cannot be processed
     */
    <T> T put(String url, String requestBody, Class<T> responseType) throws IOException;
}