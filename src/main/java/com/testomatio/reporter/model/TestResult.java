package com.testomatio.reporter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;
    private String status;
    private String message;
    private String stack;

    public static class Builder {
        private String title;
        private String testId;
        private String suiteTitle;
        private String file;
        private String status;
        private String message;
        private String stack;

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withTestId(String testId) {
            this.testId = testId;
            return this;
        }

        public Builder withSuiteTitle(String suiteTitle) {
            this.suiteTitle = suiteTitle;
            return this;
        }

        public Builder withFile(String file) {
            this.file = file;
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

        public Builder withStack(String stack) {
            this.stack = stack;
            return this;
        }

        public TestResult build() {
            return new TestResult(title, testId, suiteTitle, file, status, message, stack);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
