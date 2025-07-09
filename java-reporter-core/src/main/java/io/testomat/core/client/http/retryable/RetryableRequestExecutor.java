package io.testomat.core.client.http.retryable;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface RetryableRequestExecutor {
    HttpResponse<String> executeRetryable(HttpRequest request, HttpClient client);
}
