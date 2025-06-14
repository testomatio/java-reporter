package com.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.model.TestResult;
import java.util.List;

public interface RequestBodyBuilder {
    String buildCreateTestRunBody(String title) throws JsonProcessingException;

    String buildSingleTestReportBody(TestResult result) throws JsonProcessingException;

    String buildBatchTestReportBody(List<TestResult> results, String apiKey) throws JsonProcessingException;

    String buildFinishTestRunBody(float duration) throws JsonProcessingException;
}
