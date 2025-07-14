package io.testomat.testng.constructor;

import io.testomat.core.model.TestMetadata;
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

    private TestResultWrapper(Builder builder) {
        this.testMetadata = builder.testMetadata;
        this.status = builder.status;
        this.testResult = builder.testResult;
        this.message = builder.message;
        this.reason = builder.reason;
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
}
