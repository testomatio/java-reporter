package io.testomat.junit.reporter;

import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.constructor.JUnitTestResultConstructor;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunitTestReporter {
    private static final Logger log = LoggerFactory.getLogger(JunitTestReporter.class);
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
            log.debug("Skipping test because the run manager is not active");
            return;
        }

        TestMetadata metadata = null;
        try {
            metadata = metaDataExtractor.extractTestMetadata(context);

            TestResult result = resultConstructor.constructTestRunResult(
                    metadata, message, status, context);

            log.debug(result.toString());
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = metadata != null ? metadata.getTitle() : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }
}
