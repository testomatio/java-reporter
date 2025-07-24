package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 * Also exports test method bodies when required.
 */
public class TestNgListener implements ISuiteListener, ITestListener, IInvokedMethodListener {
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);

    private final GlobalRunManager runManager;
    private final TestNgTestResultReporter reporter;
    private final TestNgMethodExportManager methodExportManager;

    // Track processed test classes to avoid duplicate exports
    private final Set<String> processedClasses;

    public TestNgListener() {
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new TestNgTestResultReporter();
        this.methodExportManager = new TestNgMethodExportManager();
        this.processedClasses = ConcurrentHashMap.newKeySet();
    }

    /**
     * Constructor for testing
     */
    public TestNgListener(GlobalRunManager runManager,
                          TestNgTestResultReporter reporter,
                          TestNgMethodExportManager methodExportManager) {
        this.runManager = runManager;
        this.reporter = reporter;
        this.methodExportManager = methodExportManager;
        this.processedClasses = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void onStart(ISuite suite) {
        log.info("Suite started: {}", suite.getName());
        runManager.incrementSuiteCounter();
        reporter.reportTestResult(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Suite finished: {}", suite.getName());
        runManager.decrementSuiteCounter();
        // Don't export here - suite results are not ready yet
    }

    // Export after each test context finishes (when all tests in the context are done)
    @Override
    public void onFinish(ITestContext context) {
        log.info("Test context finished: {}", context.getName());

        // Process each test class in this context
        for (Class<?> testClass : context.getAllTestMethods()[0].getTestClass().getRealClass().getClasses()) {
            exportTestClassIfNotProcessed(testClass);
        }

        // Also process the main test class
        if (context.getAllTestMethods().length > 0) {
            Class<?> mainTestClass = context.getAllTestMethods()[0].getTestClass().getRealClass();
            exportTestClassIfNotProcessed(mainTestClass);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        reporter.reportTestResult(result, PASSED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        reporter.reportTestResult(result, FAILED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        reporter.reportTestResult(result, SKIPPED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
    }

    private void exportTestClassIfNotProcessed(Class<?> testClass) {
        if (testClass == null) {
            return;
        }

        String className = testClass.getName();
        if (processedClasses.add(className)) {
            log.info("Exporting test class: {}", className);
            methodExportManager.loadTestBodyForClass(testClass);
        } else {
            log.debug("Test class {} already processed", className);
        }
    }
}