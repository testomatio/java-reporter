package io.testomat.junit.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.agent.TestomatAgent;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension for Testomat.io integration.
 * Reports JUnit test execution results to Testomat.io platform.
 * Provides global execution lifecycle hooks via TestExecutionListener.
 */
public class JunitListener
        extends AbstractHookContainer
        implements BeforeEachCallback,
        BeforeAllCallback,
        AfterAllCallback,
        AfterEachCallback,
        TestWatcher,
        TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JunitListener.class);

    private final MethodExportManager methodExportManager;
    private final GlobalRunManager runManager;
    private final JunitTestReporter reporter;
    private final PropertyProvider provider;
    private final Set<String> processedClasses;
    private final FacadeFunctionsHandler functionsHandler;

    public JunitListener() {
        TestomatAgent.install();
        this.functionsHandler = new FacadeFunctionsHandler();
        this.methodExportManager = new MethodExportManager();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new JunitTestReporter();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    /**
     * Constructor for testing
     *
     * @param methodExportManager the method export manager
     * @param runManager          the global run manager
     * @param reporter            the JUnit test reporter
     * @param provider            the property provider
     */
    public JunitListener(MethodExportManager methodExportManager,
                         GlobalRunManager runManager,
                         JunitTestReporter reporter,
                         PropertyProvider provider,
                         FacadeFunctionsHandler functionsHandler) {
        this.methodExportManager = methodExportManager;
        this.runManager = runManager;
        this.reporter = reporter;
        this.provider = provider;
        this.functionsHandler = functionsHandler;
        this.processedClasses = ConcurrentHashMap.newKeySet();
    }

    @Override
    public final void beforeAll(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }
        onSuiteStartHookBeforeExecution(context);
        runManager.incrementSuiteCounter();
        onSuiteStartHookAfterExecution(context);
    }

    @Override
    public final void afterAll(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }
        onSuiteFinishHookBeforeExecution(context);
        exportTestClassIfNotProcessed(context);
        runManager.decrementSuiteCounter();
        onSuiteFinishHookAfterExecution(context);
    }

    @Override
    public final void beforeEach(ExtensionContext context) {
        beforeEachHookBeforeExecution(context);
        beforeEachHookAfterExecution(context);
    }

    @Override
    public final void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (!isListeningRequired()) {
            return;
        }
        onTestDisabledHookBeforeExecution(context, reason);
        reporter.reportTestResult(context, SKIPPED, reason.orElse("Test disabled"));
        exportTestClassIfNotProcessed(context);
        onTestDisabledHookAfterExecution(context, reason);
    }

    @Override
    public final void testSuccessful(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }
        onTestSuccessHookBeforeExecution(context);
        reporter.reportTestResult(context, PASSED, null);
        exportTestClassIfNotProcessed(context);
        onTestSuccessHookAfterExecution(context);
    }

    @Override
    public final void testAborted(ExtensionContext context, Throwable cause) {
        if (!isListeningRequired()) {
            return;
        }
        onTestAbortedHookBeforeExecution(context, cause);
        reporter.reportTestResult(context, SKIPPED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
        onTestAbortedHookAfterExecution(context, cause);
    }

    @Override
    public final void testFailed(ExtensionContext context, Throwable cause) {
        if (!isListeningRequired()) {
            return;
        }
        onTestFailureHookBeforeExecution(context, cause);
        reporter.reportTestResult(context, FAILED, cause.getMessage());
        exportTestClassIfNotProcessed(context);
        onTestFailureHookAfterExecution(context, cause);
    }

    @Override
    public final void afterEach(ExtensionContext context) {
        afterEachHookBeforeExecution(context);
        functionsHandler.handleFacadeFunctions(context);
        afterEachHookAfterExecution(context);
    }

    @Override
    public final void testPlanExecutionStarted(TestPlan testPlan) {
        onExecutionStartHookBeforeExecution(testPlan);
        log.info("JUnit test plan execution started - global initialization hook");
        onExecutionStartHookAfterExecution(testPlan);
    }

    @Override
    public final void testPlanExecutionFinished(TestPlan testPlan) {
        onExecutionFinishHookBeforeExecution(testPlan);
        log.info("JUnit test plan execution finished - global cleanup hook");
        onExecutionFinishHookAfterExecution(testPlan);
    }

    private void exportTestClassIfNotProcessed(ExtensionContext context) {
        if (!isListeningRequired()) {
            return;
        }

        Optional<Class<?>> testClass = context.getTestClass();
        if (testClass.isEmpty()) {
            return;
        }

        Class<?> clazz = testClass.get();
        String className = clazz.getName();

        if (processedClasses.add(className)) {
            methodExportManager.loadTestBodyForClass(clazz);
        }
    }

    private boolean isListeningRequired() {
        try {
            return provider.getProperty(API_KEY_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
