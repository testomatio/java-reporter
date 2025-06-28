package com.testomatio.reporter.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.exception.RequestExecutionFailedException;
import com.testomatio.reporter.exception.RequestStatusNotSuccessException;
import com.testomatio.reporter.exception.RequestUriBuildingException;
import com.testomatio.reporter.exception.ResponseJsonParsingException;
import com.testomatio.reporter.logger.LoggerUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

import static com.testomatio.reporter.constants.CommonConstants.HTTP_TIMEOUT_SECONDS;
import static com.testomatio.reporter.constants.CommonConstants.REQUEST_TIMEOUT_SECONDS;

public class NativeHttpClient implements CustomHttpClient {
    private static final String HEADER_CONTENT_NAME = "Content-Type";
    private static final String HEADER_CONTENT_VALUE = "application/json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger LOGGER = LoggerUtils.getLogger(this.getClass());
    private final HttpClient client;

    public NativeHttpClient() {
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
        return HttpRequest.newBuilder()
                .uri(createUri(url))
                .header(HEADER_CONTENT_NAME, HEADER_CONTENT_VALUE)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));
    }

    private URI createUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RequestUriBuildingException(
                    String.format("Failed to build URI from url %s, check its syntax.", url), e);
        }
    }

    private <T> T executeRequest(HttpRequest request, Class<T> responseType) {
        LOGGER.fine("Executing request: " + request);
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RequestExecutionFailedException("API request failed", e);
        }
        checkResponseStatus(response);
        return mapJsonResponse(response.body(), responseType);
    }

    private void checkResponseStatus(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            LOGGER.severe(String.format("API request failed: HTTP %s | URI: %s | Response: %s",
                    response.statusCode(), response.uri(), response.body()));
            throw new RequestStatusNotSuccessException(
                    "API request returned status code " + response.statusCode());
        }
    }

    private <T> T mapJsonResponse(String responseBody, Class<T> responseType) {
        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ResponseJsonParsingException("Failed to parse response json: " + responseBody);
        }
    }
}
