package com.testomatio.reporter.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.testomatio.reporter.model.TestResult;
import java.util.List;

/**
 * Builder for creating JSON request bodies for Testomat.io API.
 */
public interface RequestBodyBuilder {

    /**
     * Builds request body for creating test run.
     *
     * @param title test run title
     * @return JSON request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildCreateRunBody(String title) throws JsonProcessingException;

    /**
     * Builds request body for reporting single test result.
     *
     * @param result test result to report
     * @return JSON request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildSingleTestReportBody(TestResult result) throws JsonProcessingException;

    /**
     * Builds request body for reporting multiple test results.
     *
     * @param results test results to report
     * @param apiKey API key for authentication
     * @return JSON request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildBatchTestReportBody(List<TestResult> results, String apiKey) throws JsonProcessingException;

    /**
     * Builds request body for finishing test run.
     *
     * @param duration test run duration in seconds
     * @return JSON request body
     * @throws JsonProcessingException if JSON serialization fails
     */
    String buildFinishRunBody(float duration) throws JsonProcessingException;
}