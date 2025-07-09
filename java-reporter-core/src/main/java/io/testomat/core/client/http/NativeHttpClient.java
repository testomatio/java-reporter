package io.testomat.core.client.http;

import static io.testomat.core.constants.CommonConstants.HTTP_TIMEOUT_SECONDS;
import static io.testomat.core.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

import io.testomat.core.client.http.retryable.NativeRetryableRequestExecutor;
import io.testomat.core.client.http.retryable.RetryableRequestExecutor;
import io.testomat.core.client.http.util.JsonResponseMapperUtil;
import io.testomat.core.exception.RequestExecutionFailedException;
import io.testomat.core.exception.RequestUriBuildingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeHttpClient implements CustomHttpClient {
    private static final String HEADER_CONTENT_NAME = "Content-Type";
    private static final String HEADER_CONTENT_VALUE = "application/json";
    private static final Logger log = LoggerFactory.getLogger(NativeHttpClient.class);

    private final HttpClient client;
    private final RetryableRequestExecutor retryableRequestExecutor =
            new NativeRetryableRequestExecutor();

    public NativeHttpClient() {
        log.debug("Initializing Native HttpClient");
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
        log.debug("Creating Http Request");
        return HttpRequest.newBuilder()
                .uri(createUri(url))
                .header(HEADER_CONTENT_NAME, HEADER_CONTENT_VALUE)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax for request");
            throw new RequestUriBuildingException(
                    String.format("Failed to build URI from url %s, check its syntax.", url), e);
        }
    }

    private <T> T executeRequest(HttpRequest request, Class<T> responseType) {
        log.debug("Executing request: {}", request.method());

        HttpResponse<String> response;
        response = retryableRequestExecutor.executeRetryable(request, client);

        if (response == null) {
            throw new RequestExecutionFailedException("Received null response from HTTP client");
        }
        return JsonResponseMapperUtil.mapJsonResponse(response.body(), responseType);
    }
}
