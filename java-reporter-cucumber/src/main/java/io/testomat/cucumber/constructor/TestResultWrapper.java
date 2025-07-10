package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCaseFinished;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testng.ITestResult;

/**
 * Wrapper containing test metadata and framework-specific data for result construction.
 * Uses builder pattern to accommodate different test frameworks.
 */
public class TestResultWrapper {
    private final TestMetadata testMetadata;
    private final String status;
    private final ITestResult testResult;
    private final String message;
    private final String reason;
    private final TestCaseFinished cucumberTestCaseFinished;
    private final ExtensionContext junitExtensionContext;

    private TestResultWrapper(Builder builder) {
        this.testMetadata = builder.testMetadata;
        this.status = builder.status;
        this.testResult = builder.testResult;
        this.message = builder.message;
        this.reason = builder.reason;
        this.cucumberTestCaseFinished = builder.cucumberTestCaseFinished;
        this.junitExtensionContext = builder.junitExtensionContext;
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
        private ITestResult testResult;
        private String message;
        private String reason;
        private TestCaseFinished cucumberTestCaseFinished;
        private ExtensionContext junitExtensionContext;

        public Builder withTestMetadata(TestMetadata testMetadata) {
            this.testMetadata = testMetadata;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withTestResult(ITestResult testResult) {
            this.testResult = testResult;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder withCucumberTestCaseFinished(TestCaseFinished cucumberTestCaseFinished) {
            this.cucumberTestCaseFinished = cucumberTestCaseFinished;
            return this;
        }

        public Builder withJunitExtensionContext(ExtensionContext junitExtensionContext) {
            this.junitExtensionContext = junitExtensionContext;
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

    public ITestResult getTestResult() {
        return testResult;
    }

    public String getMessage() {
        return message;
    }

    public String getReason() {
        return reason;
    }

    public TestCaseFinished getCucumberTestCaseFinished() {
        return cucumberTestCaseFinished;
    }

    public ExtensionContext getJunitExtensionContext() {
        return junitExtensionContext;
    }
}
