package io.testomat.junit.reporter;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import io.testomat.junit.model.TestResultWrapper;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JunitTestReporter {
    private final JUnitTestResultConstructor resultConstructor;
    private final JunitMetaDataExtractor metaDataExtractor;
    private final GlobalRunManager runManager;

    public JunitTestReporter() {
        this.resultConstructor = new JUnitTestResultConstructor();
        this.metaDataExtractor = new JunitMetaDataExtractor();
        this.runManager = GlobalRunManager.getInstance();
    }

    /**
     * Constructor for testing
     */
    public JunitTestReporter(
            JUnitTestResultConstructor resultConstructor,
            JunitMetaDataExtractor metaDataExtractor,
            GlobalRunManager runManager) {
        this.runManager = runManager;
        this.resultConstructor = resultConstructor;
        this.metaDataExtractor = metaDataExtractor;
    }

    /**
     * Reports test result to Testomat.io platform.
     *
     * @param context JUnit extension context
     * @param status  test execution status (PASSED, FAILED, SKIPPED)
     * @param message optional message describing the result
     */
    public void reportTestResult(ExtensionContext context, String status, String message) {
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