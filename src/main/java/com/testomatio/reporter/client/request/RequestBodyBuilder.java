package com.testomatio.reporter.client.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.testomatio.reporter.model.TestCaseResult;
import java.util.List;

public interface RequestBodyBuilder {
    String buildCreateRunBody(String title) throws JsonProcessingException;

    String buildSingleTestReportBody(TestCaseResult result) throws JsonProcessingException;

    String buildBatchTestReportBody(List<TestCaseResult> results, String apiKey) throws JsonProcessingException;

    String buildFinishRunBody(float duration) throws JsonProcessingException;
}
