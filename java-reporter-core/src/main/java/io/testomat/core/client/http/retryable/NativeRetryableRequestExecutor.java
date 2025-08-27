package io.testomat.core.client.http.retryable;

import static io.testomat.core.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

import io.testomat.core.exception.RequestExecutionFailedException;
import io.testomat.core.exception.RequestStatusNotSuccessException;
import io.testomat.core.exception.RequestTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of retry logic for HTTP requests.
 * Provides exponential backoff with configurable retry attempts for transient failures
 * including network timeouts, connection errors, and specific server error status codes.
 */
public class NativeRetryableRequestExecutor implements RetryableRequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(NativeRetryableRequestExecutor.class);

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_DELAY_MS = 1000;
    private static final int MAX_DELAY_MS = 10000;
    private static final List<Integer> RETRY_STATUS_CODES = Arrays.asList(502, 503, 504);
    private static final int SUCCESS_CODE = 200;
    private static final int MAX_SUCCESS_CODE = 300;

    @Override
    public HttpResponse<String> executeRetryable(HttpRequest request, HttpClient client) {
        HttpResponse<String> lastResponse = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> retryableResponse = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
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
                log.debug("Retrying request, attempt {}/" + MAX_RETRY_ATTEMPTS, attempt);
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
        return response.statusCode() >= SUCCESS_CODE && response.statusCode() < MAX_SUCCESS_CODE;
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
