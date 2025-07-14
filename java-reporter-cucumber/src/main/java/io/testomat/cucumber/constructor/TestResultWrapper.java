package io.testomat.cucumber.constructor;

import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.model.TestMetadata;

/**
 * Wrapper containing test metadata and framework-specific data for result construction.
 * Uses builder pattern to accommodate different test frameworks.
 */
public class TestResultWrapper {
    private final TestMetadata testMetadata;
    private final String status;
    private final TestCaseFinished cucumberTestCaseFinished;

    private TestResultWrapper(Builder builder) {
        this.testMetadata = builder.testMetadata;
        this.status = builder.status;
        this.cucumberTestCaseFinished = builder.cucumberTestCaseFinished;
    }

    /**
     * Creates new builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for test case result wrapper.
     */
    public static class Builder {
        private TestMetadata testMetadata;
        private String status;
        private TestCaseFinished cucumberTestCaseFinished;

        public Builder withTestMetadata(TestMetadata testMetadata) {
            this.testMetadata = testMetadata;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withCucumberTestCaseFinished(TestCaseFinished cucumberTestCaseFinished) {
            this.cucumberTestCaseFinished = cucumberTestCaseFinished;
            return this;
        }

        public TestResultWrapper build() {
            return new TestResultWrapper(this);
        }
    }

    public TestMetadata getTestMetadata() {
        return testMetadata;
    }

    public String getStatus() {
        return status;
    }

    public TestCaseFinished getCucumberTestCaseFinished() {
        return cucumberTestCaseFinished;
    }
}
