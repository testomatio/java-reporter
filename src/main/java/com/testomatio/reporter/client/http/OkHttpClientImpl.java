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
 * HTTP client implementation using OkHttp library.
 * Configured with timeouts and automatic JSON response handling.
 */
public class OkHttpClientImpl implements CustomHttpClient {
    private final Logger LOGGER = getLogger(OkHttpClientImpl.class);

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Creates HTTP client with default timeout configuration.
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
     * Executes HTTP request and processes response with error handling.
     */
    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        LOGGER.finer("Making request to: " + request.url());

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