package com.testomatio.reporter.client.urlbuilder;

public interface UrlBuilder {
    String buildCreateRunUrl();

    String buildReportTestUrl(String testRunUid);

    String buildFinishTestRunUrl(String testRunUid);
}
