package io.testomat.junit.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

public class JUnitTestResultConstructor {

    private final JunitMetaDataExtractor metaDataExtractor;

    public JUnitTestResultConstructor() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
    }

    public JUnitTestResultConstructor(JunitMetaDataExtractor metaDataExtractor) {
        this.metaDataExtractor = metaDataExtractor;
    }

    public TestResult constructTestRunResult(TestMetadata metadata, String message,
                                             String status, ExtensionContext context) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata is null");
        }

        String stack;
        Object example = null;
        String rid = null;

        if (metaDataExtractor.isParameterizedTest(context)) {
            Map<String, Object> parameters = metaDataExtractor.extractTestParameters(context);
            if (!parameters.isEmpty()) {
                example = parameters;
                rid = metaDataExtractor.generateRid(context, parameters);
            }
        }

        if (message != null) {
            stack = extractStackTrace(context);
        } else {
            ExceptionDetails details = extractExceptionDetails(context);
            message = details.getMessage();
            stack = details.getStack();
        }

        return createTestResult(metadata, message, status, stack, example, rid);
    }

    private TestResult createTestResult(TestMetadata metadata, String message, String status,
                                        String stack, Object example, String rid) {
        TestResult.Builder builder = TestResult.builder()
                .withSuiteTitle(metadata.getSuiteTitle())
                .withTestId(metadata.getTestId())
                .withTitle(metadata.getTitle())
                .withFile(metadata.getFile())
                .withMessage(message)
                .withStatus(status)
                .withStack(stack);

        if (example != null) {
            builder.withExample(example);
        }

        if (rid != null) {
            builder.withRid(rid);
        }

        return builder.build();
    }

    private ExceptionDetails extractExceptionDetails(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    private String extractStackTrace(ExtensionContext context) {
        return Optional.ofNullable(context)
                .flatMap(ExtensionContext::getExecutionException)
                .filter(this::isReportableException)
                .map(this::getStackTrace)
                .orElse(null);
    }

    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        return new ExceptionDetails(message, stack);
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private boolean isReportableException(Throwable throwable) {
        return !(throwable instanceof TestAbortedException);
    }
}
