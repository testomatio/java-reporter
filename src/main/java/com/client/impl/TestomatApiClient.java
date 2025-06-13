package com.client.impl;

import com.client.interf.ApiInterface;
import com.constants.ApiRequestFields;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.model.TestResult;
import com.property_config.impl.PropertyProviderFactoryImpl;
import com.property_config.interf.PropertyProviderFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.constants.CommonConstants.HOST_URL_PROPERTY_NAME;
import static com.constants.CommonConstants.MEDIA_TYPE_JSON;

/**
 * Enhanced client for interacting with the Testomat.io API with batch support.
 */
public class TestomatApiClient implements ApiInterface {
    private static final PropertyProviderFactory propertyProviderFactory =
            PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private static final String API_KEY_PATH_PARAM = "api_key";
    private static final String TEST_RUN_PATH = "testrun";
    private static final String BATCH_TEST_RUN_PATH = "testrun/batch";
    private static final String RESPONSE_UID_KEY = "uid";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestomatApiClient.class);
    
    private final String hostUrl;
    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new TestomatApiClient with the provided API key.
     *
     * @param apiKey the API key for authenticating requests to Testomat.io
     */
    public TestomatApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.hostUrl = propertyProviderFactory.getPropertyProvider().getProperty(HOST_URL_PROPERTY_NAME);
    }

    /**
     * Creates a new test run in Testomat.io.
     */
    @Override
    public String createTestRun(String title) throws IOException {
        HttpUrl url = HttpUrl.parse(hostUrl).newBuilder()
                .addQueryParameter(API_KEY_PATH_PARAM, apiKey)
                .build();
        Map<String, String> body = Map.of(ApiRequestFields.TITLE, title);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, objectMapper.writeValueAsString(body)))
                .build();

        LOGGER.debug("Creating test run with title: {}", title);
        Map<String, String> responseBody = executeRequest(request, Map.class);
        assert responseBody != null;
        return responseBody.get(RESPONSE_UID_KEY);
    }

    /**
     * Reports a single test result to an existing test run in Testomat.io.
     */
    @Override
    public void reportTest(String uid, TestResult result) throws IOException {
        HttpUrl url = HttpUrl.parse(hostUrl).newBuilder()
                .addPathSegment(uid)
                .addPathSegment(TEST_RUN_PATH)
                .addQueryParameter(API_KEY_PATH_PARAM, apiKey)
                .build();
        Map<String, Object> body = getStringObjectMap(result);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, objectMapper.writeValueAsString(body)))
                .build();

        LOGGER.debug("Reporting test result for testId: {}", result.getTestId());
        executeRequest(request, null);
    }

    /**
     * Reports multiple test results in a single batch request to Testomat.io.
     * This is more efficient than sending individual requests for each test.
     */
    @Override
    public void reportTests(String uid, List<TestResult> results) throws IOException {
        if (results == null || results.isEmpty()) {
            LOGGER.debug("No test results to report");
            return;
        }

        HttpUrl url = HttpUrl.parse(hostUrl).newBuilder()
                .addPathSegment(uid)
                .addPathSegment(BATCH_TEST_RUN_PATH)
                .addQueryParameter(API_KEY_PATH_PARAM, apiKey)
                .build();

        List<Map<String, Object>> batchBody = new ArrayList<>();
        for (TestResult result : results) {
            batchBody.add(getStringObjectMap(result));
        }

        Map<String, Object> requestBody = Map.of(ApiRequestFields.TESTS, batchBody);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MEDIA_TYPE_JSON, objectMapper.writeValueAsString(requestBody)))
                .build();

        LOGGER.debug("Reporting batch of {} test results", results.size());
        executeRequest(request, null);
    }

    /**
     * Marks a test run as finished in Testomat.io.
     */
    @Override
    public void finishTestRun(String uid, float duration) throws IOException {
        HttpUrl url = HttpUrl.parse(hostUrl).newBuilder()
                .addPathSegment(uid)
                .addQueryParameter(API_KEY_PATH_PARAM, apiKey)
                .build();
        Map<String, Object> body = Map.of(
                ApiRequestFields.STATUS_EVENT, "finish",
                ApiRequestFields.DURATION, duration
        );
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(MEDIA_TYPE_JSON, objectMapper.writeValueAsString(body)))
                .build();

        LOGGER.debug("Finishing test run with uid: {}", uid);
        executeRequest(request, null);
    }

    /**
     * Executes an HTTP request and processes the response.
     */
    private <T> T executeRequest(Request request, Class<T> responseType) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";
                LOGGER.error("API request failed: HTTP {} - {} | Response: {}", 
                    response.code(), response.message(), responseBody);
                throw new IOException("API request failed: " + response.code() + " " + response.message());
            }
            if (responseType == null) {
                return null;
            }
            assert response.body() != null;
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, responseType);
        }
    }

    @NotNull
    private static Map<String, Object> getStringObjectMap(TestResult result) {
        Map<String, Object> body = new HashMap<>();
        body.put(ApiRequestFields.TITLE, result.getTitle());
        body.put(ApiRequestFields.TEST_ID, result.getTestId());
        body.put(ApiRequestFields.SUITE_TITLE, result.getSuiteTitle());
        body.put(ApiRequestFields.FILE, result.getFile());
        body.put(ApiRequestFields.STATUS, result.getStatus());
        if (result.getMessage() != null) {
            body.put(ApiRequestFields.MESSAGE, result.getMessage());
        }
        if (result.getStack() != null) {
            body.put(ApiRequestFields.STACK, result.getStack());
        }
        return body;
    }
}