package com.testomatio.reporter.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.testomatio.reporter.model.TestRunResult;
import java.util.List;

public interface RequestBodyBuilder {
    String buildCreateRunBody(String title) throws JsonProcessingException;

    String buildSingleTestReportBody(TestRunResult result) throws JsonProcessingException;

    String buildBatchTestReportBody(List<TestRunResult> results, String apiKey) throws JsonProcessingException;

    String buildFinishRunBody(float duration) throws JsonProcessingException;
}
