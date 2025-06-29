package com.testomatio.reporter.client.http.retryable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.exception.RequestExecutionFailedException;
import com.testomatio.reporter.exception.RequestStatusNotSuccessException;
import com.testomatio.reporter.exception.RequestTimeoutException;
import com.testomatio.reporter.logger.LoggerUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

public class DefaultRetryRequestExecutor implements RetryableRequestExecutor {
    private static final Logger LOGGER = LoggerUtils.getLogger(DefaultRetryRequestExecutor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 10000;
    private static final List<Integer> RETRY_STATUS_CODES = Arrays.asList(502, 503, 504);

    @Override
    public HttpResponse<String> executeRetryable(HttpRequest request, HttpClient client) {
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

    private boolean isStatusCodeOK(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RequestExecutionFailedException("Request was interrupted during retry", e);
        }
    }

    private boolean shouldRetry(HttpResponse<String> response, int attempt, Exception exception) {
        return attempt < MAX_RETRY_ATTEMPTS && (exception instanceof HttpTimeoutException
                || exception instanceof ConnectException
                || exception instanceof SocketTimeoutException
                || (response != null && RETRY_STATUS_CODES.contains(response.statusCode())));
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

    private long calculateDelay(int attempt) {
        return Math.min(INITIAL_DELAY_MS * (long) Math.pow(2, attempt - 1), MAX_DELAY_MS);
    }
}
