package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import io.testomat.junit.model.TestResultWrapper;
import io.testomat.methodloader.junit.TestLoader;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 */
public class JunitListener implements BeforeEachCallback, BeforeAllCallback,
        AfterAllCallback, TestWatcher {

    private final TestLoader testLoader = new TestLoader();
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();
    private final JUnitTestResultConstructor resultConstructor = new JUnitTestResultConstructor();
    private final JunitMetaDataExtractor metaDataExtractor = new JunitMetaDataExtractor();

    @Override
    public void beforeAll(ExtensionContext context) {
        testLoader.loadTestBodyIfRequired(context);
        runManager.incrementSuiteCounter();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        runManager.decrementSuiteCounter();
        testLoader.loadTestBodyIfRequired(context);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        reportTestResult(context, SKIPPED, reason.orElse("Test disabled"));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        reportTestResult(context, PASSED, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        reportTestResult(context, SKIPPED, cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        reportTestResult(context, FAILED, cause.getMessage());
    }

    /**
     * Reports test result to Testomat.io platform.
     *
     * @param context JUnit extension context
     * @param status  test execution status (PASSED, FAILED, SKIPPED)
     * @param message optional message describing the result
     */
    private void reportTestResult(ExtensionContext context, String status, String message) {
        if (!runManager.isActive()) {
            return;
        }

        TestMetadata metadata = null;
        try {
            metadata = metaDataExtractor.extractTestMetadata(context);

            TestResultWrapper.Builder builder = TestResultWrapper.builder()
                    .withTestMetadata(metadata)
                    .withStatus(status)
                    .withJunitExtensionContext(context);

            if (message != null) {
                builder.withMessage(message);
            }

            TestResultWrapper wrapper = builder.build();
            TestResult result = resultConstructor.constructTestRunResult(wrapper);
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }
}
