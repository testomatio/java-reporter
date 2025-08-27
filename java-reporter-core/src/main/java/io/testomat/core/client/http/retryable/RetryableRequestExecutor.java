package io.testomat.core.client.http.retryable;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Strategy interface for executing HTTP requests with retry logic.
 * Handles transient failures and implements backoff strategies for reliable API communication.
 */
public interface RetryableRequestExecutor {
    
    /**
     * Executes HTTP request with automatic retry on failures.
     * Implements retry logic for transient errors like timeouts and server errors.
     *
     * @param request HTTP request to execute
     * @param client HTTP client to use for execution
     * @return successful HTTP response
     * @throws RequestExecutionFailedException if all retry attempts fail
     * @throws RequestTimeoutException if request times out on all attempts
     * @throws RequestStatusNotSuccessException if server returns non-success status
     */
    HttpResponse<String> executeRetryable(HttpRequest request, HttpClient client);
}
