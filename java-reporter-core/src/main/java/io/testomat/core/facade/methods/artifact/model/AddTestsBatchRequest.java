package io.testomat.core.facade.methods.artifact.model;

import java.util.ArrayList;
import java.util.List;

public class AddTestsBatchRequest {

    private final String runId;
    private final String action;
    private final List<TestItem> tests;

    private AddTestsBatchRequest(Builder builder) {
        this.runId = builder.runId;
        this.action = builder.action;
        this.tests = List.copyOf(builder.tests);
    }

    public String getRunId() {
        return runId;
    }

    public String getAction() {
        return action;
    }

    public List<TestItem> getTests() {
        return tests;
    }

    public static Builder builder(String runId, String action) {
        return new Builder(runId, action);
    }

    public static class Builder {

        private final String runId;
        private final String action;
        private final List<TestItem> tests = new ArrayList<>();

        private Builder(String runId, String action) {
            this.runId = runId;
            this.action = action;
        }

        public Builder addTest(TestItem test) {
            tests.add(test);
            return this;
        }

        public Builder addTests(List<TestItem> tests) {
            this.tests.addAll(tests);
            return this;
        }

        public AddTestsBatchRequest build() {
            return new AddTestsBatchRequest(this);
        }
    }
}
