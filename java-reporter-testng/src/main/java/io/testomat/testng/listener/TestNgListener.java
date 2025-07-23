package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 */
public class TestNgListener implements ISuiteListener, ITestListener, IInvokedMethodListener {

    private final GlobalRunManager runManager;
    private final TestNgTestResultReporter reporter;

    public TestNgListener() {
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new TestNgTestResultReporter();
    }

    @Override
    public void onStart(ISuite suite) {
        runManager.incrementSuiteCounter();
        reporter.reportTestResult(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        runManager.decrementSuiteCounter();
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reporter.reportTestResult(result, PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reporter.reportTestResult(result, FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reporter.reportTestResult(result, SKIPPED);
    }
}
