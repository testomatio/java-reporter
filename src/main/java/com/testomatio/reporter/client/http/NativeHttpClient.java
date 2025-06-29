package com.testomatio.reporter.client.http;

import static com.testomatio.reporter.constants.CommonConstants.HTTP_TIMEOUT_SECONDS;
import static com.testomatio.reporter.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

import com.testomatio.reporter.client.http.retryable.DefaultRetryableRequestExecutor;
import com.testomatio.reporter.client.http.retryable.RetryableRequestExecutor;
import com.testomatio.reporter.client.http.util.JsonResponseMapperUtil;
import com.testomatio.reporter.exception.RequestExecutionFailedException;
import com.testomatio.reporter.exception.RequestUriBuildingException;
import com.testomatio.reporter.logger.LoggerUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

public class NativeHttpClient implements CustomHttpClient {
    private static final String HEADER_CONTENT_NAME = "Content-Type";
    private static final String HEADER_CONTENT_VALUE = "application/json";
    private static final Logger LOGGER = LoggerUtils.getLogger(NativeHttpClient.class);

    private final HttpClient client;
    private final RetryableRequestExecutor retryableRequestExecutor =
            new DefaultRetryableRequestExecutor();

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
        response = retryableRequestExecutor.executeRetryable(request, client);

        if (response == null) {
            throw new RequestExecutionFailedException("Received null response from HTTP client");
        }
        return JsonResponseMapperUtil.mapJsonResponse(response.body(), responseType);
    }
}
