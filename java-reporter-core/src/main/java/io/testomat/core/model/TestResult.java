package io.testomat.core.model;

public class TestResult {
    private String title;
    private String testId;
    private String suiteTitle;
    private String file;
    private String status;
    private String message;
    private String stack;
    private Object example;
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