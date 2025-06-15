package com.client.request;

import com.constants.ApiRequestFields;
import com.exception.FailedToCreateTestRunBodyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.model.TestResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constants.CommonConstants.API_KEY_STRING;
import static com.constants.CommonConstants.TESTS_STRING;

/**
 * Builder class for constructing request bodies for Testomat.io API calls.
 * Handles JSON serialization and request body structure creation.
 */
public class TestomatRequestBodyBuilder implements RequestBodyBuilder {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a new TestomatRequestBodyBuilder with default ObjectMapper.
     */
    public TestomatRequestBodyBuilder() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a new TestomatRequestBodyBuilder with custom ObjectMapper.
     *
     * @param objectMapper the ObjectMapper instance for JSON serialization
     */
    public TestomatRequestBodyBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Builds JSON request body for creating a new test run.
     *
     * @param title the title of the test run
     * @return JSON string representation of the request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    @Override
    public String buildCreateTestRunBody(String title) {
        try {
            Map<String, String> body = Map.of(ApiRequestFields.TITLE, title);
            return objectMapper.writeValueAsString(body);

        } catch (JsonProcessingException e) {
            throw new FailedToCreateTestRunBodyException("Failed to create test run body", e);
        }
    }

    /**
     * Builds JSON request body for reporting a single test result.
     *
     * @param result the test result to report
     * @return JSON string representation of the request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    @Override
    public String buildSingleTestReportBody(TestResult result) throws JsonProcessingException {
        Map<String, Object> body = buildTestResultMap(result);
        body.put("create", true);
        return objectMapper.writeValueAsString(body);
    }

    /**
     * Builds JSON request body for reporting multiple test results in batch.
     *
     * @param results the list of test results to report
     * @param apiKey  the API key for authentication
     * @return JSON string representation of the request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    @Override
    public String buildBatchTestReportBody(List<TestResult> results, String apiKey) throws JsonProcessingException {
        List<Map<String, Object>> testsArray = new ArrayList<>();
        for (TestResult result : results) {
            testsArray.add(buildTestResultMap(result));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(API_KEY_STRING, apiKey);
        requestBody.put(TESTS_STRING, testsArray);
        requestBody.put("create", true);

        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * Builds JSON request body for finishing a test run.
     *
     * @param duration the duration of the test run in seconds
     * @return JSON string representation of the request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    @Override
    public String buildFinishTestRunBody(float duration) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
                ApiRequestFields.STATUS_EVENT, "finish",
                ApiRequestFields.DURATION, duration
        );
        return objectMapper.writeValueAsString(body);
    }

    /**
     * Builds a map representation of a test result for API requests.
     *
     * @param result the test result to convert
     * @return map containing test result data
     */
    private Map<String, Object> buildTestResultMap(TestResult result) {
        Map<String, Object> body = new HashMap<>();
        body.put(ApiRequestFields.TITLE, result.getTitle());

        if (result.getTestId() != null) {
            body.put(ApiRequestFields.TEST_ID, result.getTestId());
        }

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