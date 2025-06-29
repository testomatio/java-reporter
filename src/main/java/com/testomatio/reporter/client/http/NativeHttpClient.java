package com.testomatio.reporter.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.exception.RequestExecutionFailedException;
import com.testomatio.reporter.exception.RequestStatusNotSuccessException;
import com.testomatio.reporter.exception.RequestTimeoutException;
import com.testomatio.reporter.exception.RequestUriBuildingException;
import com.testomatio.reporter.exception.ResponseJsonParsingException;
import com.testomatio.reporter.logger.LoggerUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.HTTP_TIMEOUT_SECONDS;
import static com.testomatio.reporter.constants.CommonConstants.MAX_RESPONSE_BODY_SIZE;
import static com.testomatio.reporter.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

public class NativeHttpClient implements CustomHttpClient {
    private static final String HEADER_CONTENT_NAME = "Content-Type";
    private static final String HEADER_CONTENT_VALUE = "application/json";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 10000;
    private static final List<Integer> RETRY_STATUS_CODES = Arrays.asList(502, 503, 504);


    private final Logger LOGGER = LoggerUtils.getLogger(this.getClass());
    private final HttpClient client;

    public NativeHttpClient() {
        LOGGER.fine("Initializing Native HttpClient");
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECONDS))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    public <T> T post(String url, String requestBody, Class<T> responseType) {
        HttpRequest request = createBaseRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
        return executeRequest(request, responseType);
    }

    @Override
    public <T> T put(String url, String requestBody, Class<T> responseType) {
        HttpRequest request = createBaseRequest(url)
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
        return executeRequest(request, responseType);
    }

    private HttpRequest.Builder createBaseRequest(String url) {
        LOGGER.finest("Creating Http Request");
        return HttpRequest.newBuilder()
                .uri(createUri(url))
                .header(HEADER_CONTENT_NAME, HEADER_CONTENT_VALUE)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            LOGGER.severe("Invalid URI syntax for request");
            throw new RequestUriBuildingException(
                    String.format("Failed to build URI from url %s, check its syntax.", url), e);
        }
    }

    private <T> T executeRequest(HttpRequest request, Class<T> responseType) {
        LOGGER.fine("Executing request: " + request.method());

        HttpResponse<String> response;
        response = executeRetryable(request);

        if (response == null) {
            throw new RequestExecutionFailedException("Received null response from HTTP client");
        }
        return mapJsonResponse(response.body(), responseType);
    }

    private HttpResponse<String> executeRetryable(HttpRequest request) {
        HttpResponse<String> lastResponse = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> retryableResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (isStatusCodeOK(retryableResponse)) {
                    return retryableResponse;
                }

                lastResponse = retryableResponse;
                lastException = null;
            } catch (Exception e) {
                lastException = e;
                lastResponse = null;
            }

            if (!shouldRetry(lastResponse, attempt, lastException)) {
                break;
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                LOGGER.fine("Retrying request, attempt " + attempt + "/" + MAX_RETRY_ATTEMPTS);
                sleep(calculateDelay(attempt));
            }
        }

        if (lastException != null) {
            throwOriginalException(lastException);
        } else {
            checkResponseStatus(lastResponse);
        }
        return null;
    }

    private boolean shouldRetry(HttpResponse<String> response, int attempt, Exception exception) {
        return attempt < MAX_RETRY_ATTEMPTS && (exception instanceof HttpTimeoutException
                || exception instanceof ConnectException
                || exception instanceof SocketTimeoutException
                || (response != null && RETRY_STATUS_CODES.contains(response.statusCode())));
    }

    private long calculateDelay(int attempt) {
        return Math.min(INITIAL_DELAY_MS * (long) Math.pow(2, attempt - 1), MAX_DELAY_MS);
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestExecutionFailedException("Request was interrupted during retry", e);
        }
    }

    private void throwOriginalException(Exception originalException) {
        if (originalException instanceof HttpTimeoutException) {
            throw new RequestTimeoutException(
                    String.format("Request timeout after %d seconds",
                            REQUEST_TIMEOUT_SECONDS));
        } else if (originalException instanceof IOException) {
            throw new RequestExecutionFailedException("Network error occurred", originalException);
        } else if (originalException instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            throw new RequestExecutionFailedException("Request was interrupted", originalException);
        } else {
            throw new RequestExecutionFailedException("Request failed", originalException);
        }
    }

    private void checkResponseStatus(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            throw new RequestStatusNotSuccessException(
                    "API request returned status code " + response.statusCode());
        }
    }

    private boolean isStatusCodeOK(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private <T> T mapJsonResponse(String responseBody, Class<T> responseType) {
        if (responseType == null) {
            return null;
        }
        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            String truncatedBody = responseBody != null && responseBody.length() > MAX_RESPONSE_BODY_SIZE
                    ? responseBody.substring(0, MAX_RESPONSE_BODY_SIZE) + "..."
                    : responseBody;
            throw new ResponseJsonParsingException("Failed to parse response json: " + truncatedBody);
        }
    }
}
