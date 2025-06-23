package com.testomatio.reporter.client.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.testomatio.reporter.constants.CommonConstants.HTTP_TIMEOUT_SECONDS;
import static com.testomatio.reporter.constants.CommonConstants.MEDIA_TYPE_JSON;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Implementation of HttpClient using OkHttp library.
 * Handles HTTP communication with proper timeout configuration and response processing.
 */
public class OkHttpClientImpl implements HttpClient {
    private final Logger LOGGER = getLogger(OkHttpClientImpl.class);

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new OkHttpClientImpl with default timeout settings.
     */
    public OkHttpClientImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public <T> T post(String url, String requestBody, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, requestBody))
                .build();

        return executeRequest(request, responseType);
    }

    @Override
    public <T> T put(String url, String requestBody, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(MEDIA_TYPE_JSON, requestBody))
                .build();

        return executeRequest(request, responseType);
    }

    /**
     * Executes an HTTP request and processes the response.
     *
     * @param request      the HTTP request to execute
     * @param responseType the expected response type class
     * @param <T>          the response type
     * @return the deserialized response object, or null if no response expected
     * @throws IOException if the request fails or response cannot be processed
     */
    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        getLogger(HttpClient.class).finer("Making request to: " + request.url());

        try (Response response = client.newCall(request).execute()) {
            String responseBodyString = response.body() != null ? response.body().string() : "No response body";

            if (!response.isSuccessful()) {
                LOGGER.severe(String.format("API request failed: HTTP %s - %s | URL: %s | Response: %s",
                        response.code(), response.message(), request.url(), responseBodyString));
                throw new IOException("API request failed: " + response.code() + " " + response.message());
            }

            if (responseType == null) {
                return null;
            }

            return objectMapper.readValue(responseBodyString, responseType);
        }
    }
}