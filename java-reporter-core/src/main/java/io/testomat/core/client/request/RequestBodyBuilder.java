package io.testomat.core.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.testomat.core.model.TestResult;
import java.util.List;

/**
 * Strategy interface for building JSON request bodies for Testomat.io API operations.
 * Handles the construction and serialization of request payloads for different API endpoints.
 */
public interface RequestBodyBuilder {

    /**
     * Creates JSON request body for test run creation endpoint.
     *
     * @param title descriptive title for the test run
     * @return JSON-formatted request body string
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildCreateRunBody(String title) throws JsonProcessingException;

    /**
     * Creates JSON request body for single test result reporting.
     *
     * @param result test execution result with status and metadata
     * @return JSON-formatted request body string
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildSingleTestReportBody(TestResult result) throws JsonProcessingException;

    /**
     * Creates JSON request body for batch test result reporting.
     *
     * @param results collection of test execution results
     * @param apiKey API key for authentication in batch requests
     * @return JSON-formatted request body string
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildBatchTestReportBody(List<TestResult> results, String apiKey)
            throws JsonProcessingException;

    /**
     * Creates JSON request body for test run finalization.
     *
     * @param duration total test run duration in seconds
     * @return JSON-formatted request body string
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildFinishRunBody(float duration) throws JsonProcessingException;

    String buildUploadLinksBody(String jsonString);
}
