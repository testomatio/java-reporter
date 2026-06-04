package io.testomat.core.restassured;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.net.URI;

public class TestomatHttpFilter implements Filter {

    private final boolean requestBody;
    private final boolean responseBody;
    private final boolean headers;
    private final boolean onlyFailures;

    /**
     * Creates filter with default settings.
     */
    public static TestomatHttpFilter create() {
        return new Builder().build();
    }

    /**
     * Creates builder for custom configuration.
     */
    public static Builder custom() {
        return new Builder();
    }

    private TestomatHttpFilter(Builder builder) {
        this.requestBody = builder.requestBody;
        this.responseBody = builder.responseBody;
        this.headers = builder.headers;
        this.onlyFailures = builder.onlyFailures;
    }

    @Override
    public Response filter(
        FilterableRequestSpecification request,
        FilterableResponseSpecification responseSpec,
        FilterContext ctx) {

        long startTime = System.currentTimeMillis();

        Response response = ctx.next(request, responseSpec);

        long duration = System.currentTimeMillis() - startTime;

        if (onlyFailures && response.statusCode() < 400) {
            return response;
        }

        TestStep testStep = new TestStep();
        testStep.setCategory("user");
        StepLifecycle.start(testStep);

        String summary = buildSummary(
            request,
            response,
            duration
        );

        stepWrapper("HTTP method: " + request.getMethod(), "");
        stepWrapper("URL: " + request.getURI(), "");
        stepWrapper("Status code: " + response.statusCode(), "");
        stepWrapper("Duration: " + duration + " ms", "");
        stepWrapper("Request headers: ", request.getHeaders().toString());
        stepWrapper("Response headers: ", response.getHeaders().toString());
        stepWrapper("Request body: ", response.getBody().asPrettyString());
        stepWrapper("Response body: ", response.getBody().asPrettyString());

        testStep.setStepTitle(summary);
        StepLifecycle.finish();

        return response;
    }

    private String buildSummary(FilterableRequestSpecification request, Response response, long duration) {
        String path = URI.create(request.getURI()).getPath();

        return String.format("%s %s → %d (%d ms)",
            request.getMethod(),
            path,
            response.statusCode(),
            duration
        );
    }

    private String buildDetails(
        FilterableRequestSpecification request,
        Response response,
        long duration) {

        StringBuilder sb = new StringBuilder();

        sb.append("HTTP method: ")
            .append(request.getMethod())
            .append("\n");

        sb.append("URL: ")
            .append(request.getURI())
            .append("\n");

        sb.append("Status code: ")
            .append(response.statusCode())
            .append("\n");

        sb.append("Duration: ")
            .append(duration)
            .append(" ms\n\n");

        if (headers) {
            sb.append("Request headers:\n")
                .append(request.getHeaders())
                .append("\n\n");

            sb.append("Response headers:\n")
                .append(response.getHeaders())
                .append("\n\n");
        }

        if (requestBody) {
            sb.append("Request body:\n")
                .append(request.getBody() == null
                    ? "<empty>"
                    : request.getBody().toString())
                .append("\n\n");
        }

        if (responseBody) {
            sb.append("Response body:\n")
                .append(response.getBody().asPrettyString())
                .append("\n");
        }

        return sb.toString();
    }

    public static final class Builder {

        private boolean requestBody = true;
        private boolean responseBody = true;
        private boolean headers = true;
        private boolean onlyFailures = false;

        private Builder() {
        }

        public Builder requestBody(boolean requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder responseBody(boolean responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public Builder headers(boolean headers) {
            this.headers = headers;
            return this;
        }

        public Builder onlyFailures(boolean onlyFailures) {
            this.onlyFailures = onlyFailures;
            return this;
        }

        public TestomatHttpFilter build() {
            return new TestomatHttpFilter(this);
        }
    }

    private void stepWrapper(String summary, String details) {
        TestStep testStep = new TestStep();
        testStep.setCategory("user");
        StepLifecycle.start(testStep);

        testStep.setStepTitle(summary);
        testStep.setLog(details);
        StepLifecycle.finish();
    }
}