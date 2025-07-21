package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
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

    private final MethodExportManager methodExportManager;
    private final GlobalRunManager runManager;
    private final JunitTestReporter reporter;

    public JunitListener() {
        this.methodExportManager = new MethodExportManager();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new JunitTestReporter();
    }

    /**
     * Constructor for testing
     */
    public JunitListener(MethodExportManager methodExportManager,
                         GlobalRunManager runManager,
                         JunitTestReporter reporter) {
        this.methodExportManager = methodExportManager;
        this.runManager = runManager;
        this.reporter = reporter;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        runManager.incrementSuiteCounter();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        runManager.decrementSuiteCounter();
        methodExportManager.loadTestBodyIfRequired(context);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        reporter.reportTestResult(context, SKIPPED, reason.orElse("Test disabled"));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        reporter.reportTestResult(context, PASSED, null);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        reporter.reportTestResult(context, SKIPPED, cause.getMessage());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        reporter.reportTestResult(context, FAILED, cause.getMessage());
    }
}
