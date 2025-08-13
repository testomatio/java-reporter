package io.testomat.junit.constructor;

import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestResultConstructor {

    private static final Logger log = LoggerFactory.getLogger(JUnitTestResultConstructor.class);
    private final JunitMetaDataExtractor metaDataExtractor;

    public JUnitTestResultConstructor() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
    }

    public JUnitTestResultConstructor(JunitMetaDataExtractor metaDataExtractor) {
        this.metaDataExtractor = metaDataExtractor;
    }

    /**
     * Constructs test result with enhanced parameterized test support.
     * Follows testomat.io documentation for example field format.
     */
    public TestResult constructTestRunResult(TestMetadata metadata, String message,
                                             String status, ExtensionContext context) {
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata is null");
        }

        String stack;
        Object example = null;
        String rid = null;

        // Handle parameterized tests
        if (metaDataExtractor.isParameterizedTest(context)) {
            example = metaDataExtractor.extractTestParameters(context);
            if (example != null) {
                rid = context.getUniqueId();
                log.debug("Parameterized test detected: example={}, rid={}", example, rid);
            }
        }

        // Handle exception details
        if (message != null) {
            stack = extractStackTrace(context);
        } else {
            ExceptionDetails details = extractExceptionDetails(context);
            message = details.getMessage();
            stack = details.getStack();
        }

        return createTestResult(metadata, message, status, stack, example, rid);
    }

    /**
     * Creates TestResult with conditional example and rid fields.
     */
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

        // Only add example and rid for parameterized tests
        if (example != null) {
            builder.withExample(example);
            log.trace("Added example to test result: {}", example);
        }

        if (rid != null) {
            builder.withRid(rid);
            log.trace("Added RID to test result: {}", rid);
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