package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalTestRunManager;
import com.testomatio.reporter.core.util.TestMetaDataExtractorUtil;
import com.testomatio.reporter.core.util.TestResultConstructorUtil;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

/**
 * TestNG listener implementation.
 * Implements both ISuiteListener and ITestListener to handle suite-level and test-level events.
 * Supports custom annotations (@Title, @TestId) for enhanced test metadata.
 */
public class TestNGListener implements ISuiteListener, ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestNGListener.class);
    private final GlobalTestRunManager runManager = GlobalTestRunManager.getInstance();

    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        LOGGER.debug("Started suite: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
        LOGGER.debug("Finished suite: {}", suite.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reportTestResult(result, PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reportTestResult(result, FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reportTestResult(result, SKIPPED);
    }

    private void reportTestResult(ITestResult result, String status) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            TestMetadata metadata = TestMetaDataExtractorUtil.extractTestNGTestMetadata(result);
            TestResult testResult = TestResultConstructorUtil.createTestNGTestResult(metadata, status, result);
            runManager.reportTest(testResult);
        } catch (Exception e) {
            LOGGER.error("Failed to report test result", e);
        }
    }
}