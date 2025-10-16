package io.testomat.testng.listener;

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
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import io.testomat.testng.filter.TestIdFilter;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.midi.MetaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Also exports test method bodies when required.
 * Provides global execution lifecycle hooks via IExecutionListener.
 */
public class TestNgListener implements ISuiteListener, ITestListener,
        IInvokedMethodListener, IExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);

    private final GlobalRunManager runManager;
    private final TestNgTestResultReporter reporter;
    private final TestNgMethodExportManager methodExportManager;
    private final PropertyProvider provider;

    private final Set<String> processedClasses;
    private final TestIdFilter testIdFilter;

    private final AwsService awsService;
    private final TestNgParameterExtractor testNgParameterExtractor;
    private final TestNgMetaDataExtractor metaDataExtractor;

    public TestNgListener() {
        this.metaDataExtractor = new TestNgMetaDataExtractor();
        this.testNgParameterExtractor = new TestNgParameterExtractor();
        this.methodExportManager = new TestNgMethodExportManager();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new TestNgTestResultReporter();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.testIdFilter = new TestIdFilter();
        this.awsService = new AwsService();
    }

    /**
     * Constructor for testing
     */
    public TestNgListener(TestNgMethodExportManager methodExportManager,
                          TestNgTestResultReporter reporter,
                          GlobalRunManager runManager,
                          PropertyProvider provider,
                          AwsService awsService,
                          TestIdFilter testIdFilter,
                          TestNgParameterExtractor testNgParameterExtractor,
                          TestNgMetaDataExtractor metaDataExtractor) {
        this.runManager = runManager;
        this.reporter = reporter;
        this.methodExportManager = methodExportManager;
        this.provider = provider;
        this.testNgParameterExtractor = testNgParameterExtractor;
        this.metaDataExtractor = metaDataExtractor;
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.testIdFilter = testIdFilter;
        this.awsService = awsService;
    }

    @Override
    public final void onStart(ISuite suite) {
        if (!isListeningRequired()) {
            return;
        }
        onSuiteStartHookBeforeExecution(suite);
        log.debug("Suite started: {}", suite.getName());
        runManager.incrementSuiteCounter();
        reporter.reportTestResult(suite);
        onSuiteStartHookAfterExecution(suite);
    }

    @Override
    public final void onFinish(ISuite suite) {
        if (!isListeningRequired()) {
            return;
        }
        onSuiteFinishHookBeforeExecution(suite);
        log.debug("Suite finished: {}", suite.getName());
        runManager.decrementSuiteCounter();
        onSuiteFinishHookAfterExecution(suite);
    }

    @Override
    public final void onFinish(ITestContext context) {
        if (!isListeningRequired()) {
            return;
        }
        log.debug("Test context finished: {}", context.getName());

        for (Class<?> testClass : context.getAllTestMethods()[0]
                .getTestClass()
                .getRealClass()
                .getClasses()) {
            exportTestClassIfNotProcessed(testClass);
        }

        if (context.getAllTestMethods().length > 0) {
            Class<?> mainTestClass = context.getAllTestMethods()[0].getTestClass().getRealClass();
            exportTestClassIfNotProcessed(mainTestClass);
        }
    }

    @Override
    public final void onTestStart(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        onTestStartHookBeforeExecution(result);
        testIdFilter.filterTest(result);
        onTestStartHookAfterExecution(result);
    }

    @Override
    public final void onTestSuccess(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        onTestSuccessHookBeforeExecution(result);
        reporter.reportTestResult(result, PASSED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
        onTestSuccessHookAfterExecution(result);
    }

    @Override
    public final void onTestFailure(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        onTestFailureHookBeforeExecution(result);
        reporter.reportTestResult(result, FAILED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
        onTestFailureHookAfterExecution(result);
    }

    @Override
    public final void onTestSkipped(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        onTestSkippedHookBeforeExecution(result);
        reporter.reportTestResult(result, SKIPPED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
        onTestSkippedHookAfterExecution(result);
    }

    @Override
    public final void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        afterInvocationHookBeforeExecution(method, testResult);
        if (method.isTestMethod() && !defineArtifactsDisabled()) {
            awsService.uploadAllArtifactsForTest(testResult.getName(),
                    testNgParameterExtractor.generateRid(testResult),
                    metaDataExtractor.getTestId(
                            method.getTestMethod().getConstructorOrMethod().getMethod())
            );
        }
        afterInvocationHookAfterExecution(method, testResult);
    }

    @Override
    public final void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        beforeInvocationHookBeforeExecution(method, testResult);
        beforeInvocationHookAfterExecution(method, testResult);
    }

    @Override
    public final void onExecutionStart() {
        onExecutionStartHookBeforeExecution();
        log.info("TestNG execution started - global initialization hook");
        onExecutionStartHookAfterExecution();
    }

    @Override
    public final void onExecutionFinish() {
        onExecutionFinishHookBeforeExecution();
        log.info("TestNG execution finished - global cleanup hook");
        onExecutionFinishHookAfterExecution();
    }

    protected void onSuiteStartHookAfterExecution(ISuite suite) {
    }

    protected void onSuiteStartHookBeforeExecution(ISuite suite) {
    }

    protected void onSuiteFinishHookAfterExecution(ISuite suite) {
    }

    protected void onSuiteFinishHookBeforeExecution(ISuite suite) {
    }

    protected void onTestSuccessHookAfterExecution(ITestResult result) {
    }

    protected void onTestSuccessHookBeforeExecution(ITestResult result) {
    }

    protected void onTestFailureHookAfterExecution(ITestResult result) {
    }

    protected void onTestFailureHookBeforeExecution(ITestResult result) {
    }

    protected void onTestSkippedHookAfterExecution(ITestResult result) {
    }

    protected void onTestSkippedHookBeforeExecution(ITestResult result) {
    }

    protected void onTestStartHookAfterExecution(ITestResult result) {
    }

    protected void onTestStartHookBeforeExecution(ITestResult result) {
    }

    protected void beforeInvocationHookAfterExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
    }

    protected void beforeInvocationHookBeforeExecution(IInvokedMethod method,
                                                       ITestResult testResult) {
    }

    protected void afterInvocationHookAfterExecution(IInvokedMethod method,
                                                     ITestResult testResult) {
    }

    protected void afterInvocationHookBeforeExecution(IInvokedMethod method,
                                                      ITestResult testResult) {
    }

    protected void onExecutionStartHookAfterExecution() {
    }

    protected void onExecutionStartHookBeforeExecution() {
    }

    protected void onExecutionFinishHookAfterExecution() {
    }

    protected void onExecutionFinishHookBeforeExecution() {
    }

    private void exportTestClassIfNotProcessed(Class<?> testClass) {
        if (testClass == null) {
            return;
        }

        String className = testClass.getName();
        if (processedClasses.add(className)) {
            log.debug("Exporting test class: {}", className);
            methodExportManager.loadTestBodyForClass(testClass);
        } else {
            log.debug("Test class {} already processed", className);
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

    private void handleMetaAfterInvocation(ITestResult testResult) {
        MetaStorage.LINKED_META_STORAGE.put(
                testNgParameterExtractor.generateRid(testResult), MetaStorage.TEMP_META_STORAGE.get());
    }
}
