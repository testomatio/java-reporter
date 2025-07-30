package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 */
public class JunitListener implements BeforeEachCallback, BeforeAllCallback,
        AfterAllCallback, TestWatcher {

    private static final Logger log = LoggerFactory.getLogger(JunitListener.class);

    private final MethodExportManager methodExportManager;
    private final GlobalRunManager runManager;
    private final JunitTestReporter reporter;

    private final Set<String> processedClasses;

    public JunitListener() {
        this.methodExportManager = new MethodExportManager();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new JunitTestReporter();
        this.processedClasses = ConcurrentHashMap.newKeySet();
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
        this.processedClasses = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        runManager.incrementSuiteCounter();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        exportTestClassIfNotProcessed(context);
        runManager.decrementSuiteCounter();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        reporter.reportTestResult(context, SKIPPED, reason.orElse("Test disabled"));
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        reporter.reportTestResult(context, PASSED, null);
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        reporter.reportTestResult(context, SKIPPED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        reporter.reportTestResult(context, FAILED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
    }

    private void exportTestClassIfNotProcessed(ExtensionContext context) {
        Optional<Class<?>> testClass = context.getTestClass();
        if (testClass.isEmpty()) {
            return;
        }

        Class<?> clazz = testClass.get();
        String className = clazz.getName();

        if (processedClasses.add(className)) {
            log.debug("Exporting test class: {}", className);
            methodExportManager.loadTestBodyForClass(clazz);
        } else {
            log.debug("Test class {} already processed", className);
        }
    }
}
