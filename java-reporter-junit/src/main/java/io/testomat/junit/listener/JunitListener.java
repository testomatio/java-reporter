package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.artifact.client.AwsService;
import io.testomat.core.meta.MetaStorage;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import io.testomat.junit.methodexporter.MethodExportManager;
import io.testomat.junit.reporter.JunitTestReporter;
import java.util.Map;
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
public class JunitListener implements BeforeEachCallback, BeforeAllCallback,
        AfterAllCallback, AfterEachCallback, TestWatcher, TestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JunitListener.class);
    private boolean artifactDisabled = false;

    private final MethodExportManager methodExportManager;
    private final GlobalRunManager runManager;
    private final JunitTestReporter reporter;
    private final PropertyProvider provider;
    private final AwsService awsService;
    private final Set<String> processedClasses;

    public JunitListener() {
        this.methodExportManager = new MethodExportManager();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new JunitTestReporter();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.awsService = new AwsService();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.artifactDisabled = defineArtifactsDisabled();
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
                         AwsService awsService) {
        this.methodExportManager = methodExportManager;
        this.runManager = runManager;
        this.reporter = reporter;
        this.provider = provider;
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.awsService = awsService;
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
        if (!artifactDisabled) {
            awsService.uploadAllArtifactsForTest(context.getDisplayName(), context.getUniqueId(),
                    JunitMetaDataExtractor.extractTestId(context.getTestMethod().get()));
        }
        handleMetaAfterEach(context);
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

    protected void onSuiteStartHookAfterExecution(ExtensionContext context) {
    }

    protected void onSuiteStartHookBeforeExecution(ExtensionContext context) {
    }

    protected void onSuiteFinishHookAfterExecution(ExtensionContext context) {
    }

    protected void onSuiteFinishHookBeforeExecution(ExtensionContext context) {
    }

    protected void beforeEachHookAfterExecution(ExtensionContext context) {
    }

    protected void beforeEachHookBeforeExecution(ExtensionContext context) {
    }

    protected void onTestSuccessHookAfterExecution(ExtensionContext context) {
    }

    protected void onTestSuccessHookBeforeExecution(ExtensionContext context) {
    }

    protected void onTestFailureHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestFailureHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestDisabledHookAfterExecution(ExtensionContext context,
                                                    Optional<String> reason) {
    }

    protected void onTestDisabledHookBeforeExecution(ExtensionContext context,
                                                     Optional<String> reason) {
    }

    protected void onTestAbortedHookAfterExecution(ExtensionContext context, Throwable cause) {
    }

    protected void onTestAbortedHookBeforeExecution(ExtensionContext context, Throwable cause) {
    }

    protected void afterEachHookAfterExecution(ExtensionContext context) {
    }

    protected void afterEachHookBeforeExecution(ExtensionContext context) {
    }

    protected void onExecutionStartHookAfterExecution(TestPlan testPlan) {
    }

    protected void onExecutionStartHookBeforeExecution(TestPlan testPlan) {
    }

    protected void onExecutionFinishHookAfterExecution(TestPlan testPlan) {
    }

    protected void onExecutionFinishHookBeforeExecution(TestPlan testPlan) {
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

    private boolean defineArtifactsDisabled() {
        boolean result;
        String property;
        try {
            property = provider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME);
            result = property != null
                    && !property.trim().isEmpty()
                    && !property.equalsIgnoreCase("0");

        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private boolean isListeningRequired() {
        try {
            return provider.getProperty(API_KEY_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleMetaAfterEach(ExtensionContext context) {
        String rid = JunitMetaDataExtractor.extractTestId(context.getTestMethod().get());
        Map<String, String> metaData = MetaStorage.TEMP_META_STORAGE.get();

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
        }
    }
}
