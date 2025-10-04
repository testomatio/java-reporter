package io.testomat.junit.constructor;

import io.testomat.core.StepStorage;
import io.testomat.core.TestStep;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.exception.ReporterException;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs test result objects from JUnit test execution context and metadata.
 * This class is responsible for building {@link TestResult} instances by extracting
 * relevant information from JUnit's {@link ExtensionContext}, including test metadata,
 * execution status, exception details, and parameterized test data.
 */
public class JUnitTestResultConstructor {

    private static final Logger log = LoggerFactory.getLogger(JUnitTestResultConstructor.class);

    private static final Pattern TESTOMAT_API_KEY_PATTERN = Pattern.compile(
            "\\btstmt_[a-zA-Z0-9_-]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern WINDOWS_PATH_PATTERN = Pattern.compile(
            "[C-Z]:\\\\[^\\s]+\\\\([^\\\\\\s]+)");
    private static final Pattern UNIX_PATH_PATTERN = Pattern.compile(
            "/[^\\s]+/([^/\\s]+)");

    private final JunitMetaDataExtractor metaDataExtractor;

    /**
     * Creates a new JUnitTestResultConstructor with a default metadata extractor.
     */
    public JUnitTestResultConstructor() {
        this.metaDataExtractor = new JunitMetaDataExtractor();
    }

    /**
     * Creates a new JUnitTestResultConstructor with the specified metadata extractor.
     *
     * @param metaDataExtractor the metadata extractor to use for extracting test information
     * @throws NullPointerException if metaDataExtractor is null
     */
    public JUnitTestResultConstructor(JunitMetaDataExtractor metaDataExtractor) {
        // REVIEW: Exemplary defensive programming - null check with meaningful message
        this.metaDataExtractor = Objects.requireNonNull(metaDataExtractor,
                "metaDataExtractor cannot be null");
    }

    /**
     * Constructs a test result from the provided metadata and execution context.
     * This method processes both regular and parameterized tests, extracting exception
     * details when available and preserving test parameters for parameterized tests.
     *
     * @param metadata the test metadata containing suite title, test ID, title, and file
     * @param message  the test message (if null, will be extracted from context exception)
     * @param status   the test execution status (e.g., "passed", "failed", "skipped")
     * @param context  the JUnit extension context containing execution details
     * @return a complete TestResult object with all available information
     */
    public TestResult constructTestRunResult(TestMetadata metadata,
                                             String message,
                                             String status,
                                             ExtensionContext context) {
        Objects.requireNonNull(metadata, "metadata cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        String stack;
        Object example = null;
        String rid = context.getUniqueId();
        log.debug("-> RID = {}", rid);

        if (metaDataExtractor.isParameterizedTest(context)) {
            example = metaDataExtractor.extractTestParameters(context);
            rid = context.getUniqueId();
            log.debug("Parameterized test - example: {}, rid: {}", example, rid);
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

    /**
     * Creates a TestResult object using the provided parameters.
     * Collects any steps stored in ThreadLocal storage and includes them in the result.
     *
     * @param metadata the test metadata
     * @param message  the test message
     * @param status   the test status
     * @param stack    the stack trace (if any)
     * @param example  the test parameters for parameterized tests (if any)
     * @param rid      the unique identifier for parameterized test runs (if any)
     * @return a complete TestResult object
     */
    private TestResult createTestResult(TestMetadata metadata,
                                        String message,
                                        String status,
                                        String stack,
                                        Object example,
                                        String rid) {
        Objects.requireNonNull(metadata, "metadata cannot be null");

        // Collect steps from ThreadLocal storage
        List<TestStep> steps = StepStorage.getSteps();
        log.info("Collecting steps for test '{}': {} steps found",
                metadata.getTitle(), steps.size());

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

        if (!steps.isEmpty()) {
            builder.withSteps(steps);
        }

        // Clear steps after collecting them
        StepStorage.clear();

        return builder.build();
    }

    /**
     * Extracts exception details from the test execution context.
     *
     * @param context the extension context
     * @return exception details if a reportable exception exists, empty details otherwise
     */
    private ExceptionDetails extractExceptionDetails(ExtensionContext context) {
        return extractReportableException(context)
                .map(this::createExceptionDetails)
                .orElse(ExceptionDetails.empty());
    }

    /**
     * Extracts the stack trace from the test execution context.
     *
     * @param context the extension context
     * @return the stack trace string if a reportable exception exists, null otherwise
     */
    private String extractStackTrace(ExtensionContext context) {
        return extractReportableException(context)
                .map(this::getStackTrace)
                .orElse(null);
    }

    /**
     * Extracts a reportable exception from the execution context.
     * Filters out test aborted exceptions as they are not considered reportable.
     *
     * @param context the extension context
     * @return an optional containing the reportable exception if present
     */
    private Optional<Throwable> extractReportableException(ExtensionContext context) {
        return context.getExecutionException()
                .filter(this::isReportableException);
    }

    /**
     * Creates exception details from a throwable.
     *
     * @param throwable the throwable to process
     * @return exception details containing message and stack trace
     * @throws NullPointerException if throwable is null
     */
    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable cannot be null");
        String message = sanitizeSensitiveContent(throwable.getMessage());
        String stack = getStackTrace(throwable);
        return new ExceptionDetails(message, stack);
    }

    /**
     * Sanitizes text content by removing sensitive information.
     * Specifically removes Testomat API keys and simplifies file paths.
     *
     * @param text the raw text to sanitize
     * @return sanitized text with sensitive information removed
     */
    private String sanitizeSensitiveContent(String text) {
        if (text == null) {
            return null;
        }

        String sanitized = text;

        sanitized = TESTOMAT_API_KEY_PATTERN.matcher(sanitized).replaceAll("tstmt_***");

        sanitized = WINDOWS_PATH_PATTERN.matcher(sanitized).replaceAll("$1");
        sanitized = UNIX_PATH_PATTERN.matcher(sanitized).replaceAll("$1");

        return sanitized;
    }

    /**
     * Converts a throwable to its string representation including stack trace.
     *
     * @param throwable the throwable to convert
     * @return the complete stack trace as a string
     * @throws ReporterException if an error occurs while extracting the stack trace
     */
    private String getStackTrace(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            String rawStackTrace = sw.toString();
            return sanitizeSensitiveContent(rawStackTrace);
        } catch (Exception e) {
            throw new ReporterException("Failed to get stack trace", e);
        }
    }

    /**
     * Determines if an exception should be reported.
     * Test aborted exceptions are considered non-reportable.
     *
     * @param throwable the throwable to check
     * @return true if the exception should be reported, false otherwise
     * @throws NullPointerException if throwable is null
     */
    private boolean isReportableException(Throwable throwable) {
        return !(Objects.requireNonNull(throwable) instanceof TestAbortedException);
    }
}
