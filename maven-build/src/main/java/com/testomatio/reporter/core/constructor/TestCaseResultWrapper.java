package com.testomatio.reporter.core.constructor;

import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCaseFinished;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testng.ITestResult;

@Getter
public class TestCaseResultWrapper {
    private final TestMetadata testMetadata;
    private final String status;
    private final ITestResult testResult;
    private final String message;
    private final String reason;
    private final TestCaseFinished cucumberTestCaseFinished;
    private final ExtensionContext jUnitExtensionContext;

    private TestCaseResultWrapper(Builder builder) {
        this.testMetadata = builder.testMetadata;
        this.status = builder.status;
        this.testResult = builder.testResult;
        this.message = builder.message;
        this.reason = builder.reason;
        this.cucumberTestCaseFinished = builder.cucumberTestCaseFinished;
        this.jUnitExtensionContext = builder.jUnitExtensionContext;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TestMetadata testMetadata;
        private String status;
        private ITestResult testResult;
        private String message;
        private String reason;
        private TestCaseFinished cucumberTestCaseFinished;
        private ExtensionContext jUnitExtensionContext;

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

        public Builder withJUnitExtensionContext(ExtensionContext jUnitExtensionContext) {
            this.jUnitExtensionContext = jUnitExtensionContext;
            return this;
        }

        public TestCaseResultWrapper build() {
            return new TestCaseResultWrapper(this);
        }
    }
}