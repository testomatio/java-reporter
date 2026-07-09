package io.testomat.core.restassured;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.testomat.core.facade.Testomatio;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.TestStep;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestomatHttpFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TestomatHttpFilter.class);

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
    public static Builder builder() {
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

        reportHttpData("Method: " + request.getMethod(), null);
        reportHttpData("URL: " + request.getURI(), null);
        reportHttpData("Status: " + response.statusCode(), null);
        reportHttpData("Duration: " + duration + " ms", null);

        if (headers) {
            reportHttpData("Request headers", request.getHeaders().toString());
            reportHttpData("Response headers", response.getHeaders().toString());
        }

        if (requestBody) {
            reportHttpData("Request body",
                request.getBody() == null ? "<empty>" : request.getBody().toString());
        }

        if (responseBody) {
            reportHttpData("Response body", response.getBody().asPrettyString());
        }

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

    private void reportHttpData(String summary, String details) {
        TestStep testStep = new TestStep();
        testStep.setCategory("user");
        StepLifecycle.start(testStep);
        testStep.setStepTitle(summary);

        try {
            if (details != null && !details.isBlank()) {
                Path dir = Paths.get("target", "testomat", "http");
                Files.createDirectories(dir);
                Path json = Files.createTempFile(dir, "http-", ".json");
                Files.writeString(json, details, StandardCharsets.UTF_8);
                Testomatio.stepArtifact(json.toString());
            }
        } catch (IOException e) {
            log.error("Failed to create HTTP attachment", e);
        } finally {
            StepLifecycle.finish();
        }
    }
}