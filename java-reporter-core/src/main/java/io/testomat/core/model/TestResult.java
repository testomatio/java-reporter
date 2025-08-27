package io.testomat.core.model;

/**
 * Represents a test execution result with metadata for Testomat.io reporting.
 * Contains test outcome, diagnostic information, and optional parameterized test data.
 * 
 * <p>Use the {@link Builder} for convenient construction:
 * <pre>{@code
 * TestResult result = TestResult.builder()
 *     .withTitle("User Login Test")
 *     .withStatus("passed")
 *     .withSuiteTitle("Authentication Tests")
 *     .withFile("LoginTest.java")
 *     .build();
 * }</pre>
 */
public class TestResult {
    /** Human-readable test method or scenario name */
    private String title;
    
    /** Unique test identifier from Testomat.io test management system */
    private String testId;
    
    /** Test suite or test class name containing this test */
    private String suiteTitle;
    
    /** Source file path where the test is located */
    private String file;
    
    /** Test execution status: "passed", "failed", or "skipped" */
    private String status;
    
    /** Error or failure message for failed tests, null for passed tests */
    private String message;
    
    /** Stack trace for failed tests, null for passed tests */
    private String stack;
    
    /** Parameterized test data or example values for data-driven tests */
    private Object example;
    
    /** Run identifier for associating results with specific test execution runs */
    private String rid;

    public TestResult() {
    }

    public TestResult(String title, String testId,
                      String suiteTitle, String file,
                      String status, String message, String stack,
                      Object example, String rid) {
        this.title = title;
        this.testId = testId;
        this.suiteTitle = suiteTitle;
        this.file = file;
        this.status = status;
        this.message = message;
        this.stack = stack;
        this.example = example;
        this.rid = rid;
    }

    /**
     * Builder for constructing TestResult instances with fluent API.
     * Provides convenient method chaining for setting test result properties.
     */
    public static class Builder {
        private String title;
        private String testId;
        private String suiteTitle;
        private String file;
        private String status;
        private String message;
        private String stack;
        private Object example;
        private String rid;

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

        public Builder withExample(Object example) {
            this.example = example;
            return this;
        }

        public Builder withRid(String rid) {
            this.rid = rid;
            return this;
        }

        public TestResult build() {
            return new TestResult(title, testId, suiteTitle, file, status, message, stack, example, rid);
        }
    }

    /**
     * Creates a new TestResult builder instance.
     * 
     * @return new Builder for constructing TestResult objects
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getSuiteTitle() {
        return suiteTitle;
    }

    public void setSuiteTitle(String suiteTitle) {
        this.suiteTitle = suiteTitle;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }
}