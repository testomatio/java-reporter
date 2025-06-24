package com.testomatio.reporter.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.constants.ApiRequestFields;
import com.testomatio.reporter.exception.FailedToCreateRunBodyException;
import com.testomatio.reporter.model.TestResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;

import static com.testomatio.reporter.constants.CommonConstants.API_KEY_STRING;
import static com.testomatio.reporter.constants.CommonConstants.TESTS_STRING;

/**
 * JSON request body builder for Testomat.io API operations.
 * Handles serialization and structure creation for all API endpoints.
 */
@EqualsAndHashCode
public class TestomatRequestBodyBuilder implements RequestBodyBuilder {

    private final ObjectMapper objectMapper;

    public TestomatRequestBodyBuilder() {
        this.objectMapper = new ObjectMapper();
    }

    public TestomatRequestBodyBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String buildCreateRunBody(String title) {
        try {
            Map<String, String> body = Map.of(ApiRequestFields.TITLE, title);
            return objectMapper.writeValueAsString(body);

        } catch (JsonProcessingException e) {
            throw new FailedToCreateRunBodyException("Failed to create test run body", e);
        }
    }

    @Override
    public String buildSingleTestReportBody(TestResult result) throws JsonProcessingException {
        Map<String, Object> body = buildTestResultMap(result);
        body.put("create", "true");
        return objectMapper.writeValueAsString(body);
    }

    @Override
    public String buildBatchTestReportBody(List<TestResult> results, String apiKey) throws JsonProcessingException {
        List<Map<String, Object>> testsArray = new ArrayList<>();
        for (TestResult result : results) {
            testsArray.add(buildTestResultMap(result));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(API_KEY_STRING, apiKey);
        requestBody.put(TESTS_STRING, testsArray);
        requestBody.put("create", "true");

        return objectMapper.writeValueAsString(requestBody);
    }

    @Override
    public String buildFinishRunBody(float duration) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
                ApiRequestFields.STATUS_EVENT, "finish",
                ApiRequestFields.DURATION, duration
        );
        return objectMapper.writeValueAsString(body);
    }

    /**
     * Converts test result to map for JSON serialization.
     */
    private Map<String, Object> buildTestResultMap(TestResult result) {
        Map<String, Object> body = new HashMap<>();
        body.put("create", "true");
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