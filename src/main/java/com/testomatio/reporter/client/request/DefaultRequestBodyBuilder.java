package com.testomatio.reporter.client.request;

import static com.testomatio.reporter.constants.CommonConstants.API_KEY_STRING;
import static com.testomatio.reporter.constants.CommonConstants.TESTS_STRING;
import static com.testomatio.reporter.constants.PropertyNameConstants.CREATE_TEST_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.ENVIRONMENT_PROPERTY_NAME;
import static com.testomatio.reporter.constants.PropertyNameConstants.RUN_GROUP_PROPERTY_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.constants.ApiRequestFields;
import com.testomatio.reporter.exception.FailedToCreateRunBodyException;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.propertyconfig.impl.PropertyProviderFactoryImpl;
import com.testomatio.reporter.propertyconfig.interf.PropertyProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON request body builder for Testomat.io API operations.
 * Handles serialization and structure creation for all API endpoints.
 */
public class DefaultRequestBodyBuilder implements RequestBodyBuilder {
    private final String createParam;

    private final ObjectMapper objectMapper;
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    public DefaultRequestBodyBuilder() {
        this.objectMapper = new ObjectMapper();
        this.createParam = getCreateParam();
    }

    public DefaultRequestBodyBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.createParam = getCreateParam();
    }

    @Override
    public String buildCreateRunBody(String title) {
        try {
            Map<String, String> body = new HashMap<>(Map.of(ApiRequestFields.TITLE, title));
            String customEnv = getCustomEnvironment();
            if (customEnv != null) {
                body.put(ApiRequestFields.ENVIRONMENT, customEnv);
            }
            String groupTitle = getRunGroupTitle();
            if (groupTitle != null) {
                body.put(ApiRequestFields.GROUP_TITLE, groupTitle);
            }
            return objectMapper.writeValueAsString(body);

        } catch (JsonProcessingException e) {
            throw new FailedToCreateRunBodyException("Failed to create test run body", e);
        }
    }

    @Override
    public String buildSingleTestReportBody(TestResult result) throws JsonProcessingException {
        Map<String, Object> body = buildTestResultMap(result);
        return objectMapper.writeValueAsString(body);
    }

    @Override
    public String buildBatchTestReportBody(List<TestResult> results, String apiKey)
            throws JsonProcessingException {
        List<Map<String, Object>> testsArray = new ArrayList<>();
        for (TestResult result : results) {
            testsArray.add(buildTestResultMap(result));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(API_KEY_STRING, apiKey);
        requestBody.put(TESTS_STRING, testsArray);

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
        if (this.createParam != null) {
            body.put("create","true");
        }

        return body;
    }

    private String getCustomEnvironment() {
        try {
            return provider.getProperty(ENVIRONMENT_PROPERTY_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    private String getRunGroupTitle() {
        try {
            return provider.getProperty(RUN_GROUP_PROPERTY_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCreateParam() {
        try {
            return provider.getProperty(CREATE_TEST_PROPERTY_NAME);
        } catch (Exception e) {
            return null;
        }
    }
}
