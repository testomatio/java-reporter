package io.testomat.junit.model;

import io.testomat.core.model.TestMetadata;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Wrapper containing test metadata and framework-specific data for result construction.
 * Uses builder pattern to accommodate different test frameworks.
 */
public class TestResultWrapper {
    private final TestMetadata testMetadata;
    private final String status;
    private final String message;
    private final ExtensionContext junitExtensionContext;

    private TestResultWrapper(Builder builder) {
        this.testMetadata = builder.testMetadata;
        this.status = builder.status;
        this.message = builder.message;
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
        private String message;
        private ExtensionContext junitExtensionContext;

        public Builder withTestMetadata(TestMetadata testMetadata) {
            this.testMetadata = testMetadata;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
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

    public String getMessage() {
        return message;
    }

    public ExtensionContext getJunitExtensionContext() {
        return junitExtensionContext;
    }
}
