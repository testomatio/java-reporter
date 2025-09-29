package io.testomat.core.client.request;

import static io.testomat.core.constants.CommonConstants.API_KEY_STRING;
import static io.testomat.core.constants.CommonConstants.TESTS_STRING;
import static io.testomat.core.constants.PropertyNameConstants.CREATE_TEST_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.ENVIRONMENT_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.PUBLISH_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.RUN_GROUP_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.SHARED_RUN_PROPERTY_NAME;
import static io.testomat.core.constants.PropertyNameConstants.SHARED_TIMEOUT_PROPERTY_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.core.artifact.ReportedTestStorage;
import io.testomat.core.constants.ApiRequestFields;
import io.testomat.core.exception.FailedToCreateRunBodyException;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link RequestBodyBuilder} for Testomat.io API requests.
 * Handles JSON serialization and payload structure creation for all API endpoints
 * with support for parameterized tests, shared runs, and configurable properties.
 */
public class NativeRequestBodyBuilder implements RequestBodyBuilder {
    private static final String TRUE = "true";
    private final Boolean createParam;
    private final String sharedRun;
    private final String sharedRunTimeout;
    private final String publishParam;

    private final ObjectMapper objectMapper;
    private final PropertyProvider provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();

    public NativeRequestBodyBuilder() {
        this.publishParam = getPropertySafely(PUBLISH_PROPERTY_NAME);
        this.sharedRun = getSharedRun();
        this.sharedRunTimeout = getPropertySafely(SHARED_TIMEOUT_PROPERTY_NAME);
        this.objectMapper = new ObjectMapper();
        this.createParam = getCreateParam();
    }

    @Override
    public String buildCreateRunBody(String title) {
        try {
            Map<String, String> body = new HashMap<>(Map.of(ApiRequestFields.TITLE, title));
            String customEnv = getPropertySafely(ENVIRONMENT_PROPERTY_NAME);
            if (customEnv != null) {
                body.put(ApiRequestFields.ENVIRONMENT, customEnv);
            }
            String groupTitle = getPropertySafely(RUN_GROUP_PROPERTY_NAME);
            if (groupTitle != null) {
                body.put(ApiRequestFields.GROUP_TITLE, groupTitle);
            }
            if (this.sharedRun != null) {
                body.put("shared_run", sharedRun);

            }
            if (this.sharedRunTimeout != null) {
                body.put("shared_run_timeout", sharedRunTimeout);

            }
            if (this.publishParam != null) {
                body.put("access_event", "publish");
            }

            body.put("overwrite", "true");

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
    public String buildBatchReportBodyWithArtifacts(List<Map<String, Object>> testsWithArtifacts,
                                                    String apiKey) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(API_KEY_STRING, apiKey);
        requestBody.put(TESTS_STRING, testsWithArtifacts);

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

    @Override
    public String buildUploadLinksBody(String jsonString) {
        return "";
    }

    /**
     * Converts test result to map structure for JSON serialization.
     * Includes all standard fields plus support for parameterized test data.
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

        if (result.getExample() != null) {
            body.put("example", result.getExample());
        }

        if (result.getRid() != null) {
            body.put("rid", result.getRid());
        }

        if (createParam) {
            body.put("create", TRUE);
        }

        body.put("overwrite", "true");
        ReportedTestStorage.store(body);
        return body;
    }

    /**
     * Safely retrieves property value, returning null if property is not
     * found or any exception occurs.
     * Centralizes exception handling for all property access operations.
     *
     * @param propertyName the name of the property to retrieve
     * @return property value or null if not found/error occurs
     */
    private String getPropertySafely(String propertyName) {
        try {
            return provider.getProperty(propertyName);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean getCreateParam() {
        try {
            return provider.getProperty(CREATE_TEST_PROPERTY_NAME).equalsIgnoreCase(TRUE);
        } catch (Exception e) {
            return false;
        }
    }

    private String getSharedRun() {
        String property;
        try {
            property = provider.getProperty(SHARED_RUN_PROPERTY_NAME);
            if (property != null
                    && !property.trim().isEmpty()
                    && !property.equalsIgnoreCase("0")
            ) {
                return property;
            }
        } catch (Exception e) {
            return null;
        }
        return property;
    }
}