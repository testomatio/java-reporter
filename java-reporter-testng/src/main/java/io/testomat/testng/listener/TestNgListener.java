package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;

import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import io.testomat.testng.filter.TestIdFilter;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IClass;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Also exports test method bodies when required.
 * Provides global execution lifecycle hooks via IExecutionListener.
 */
public class TestNgListener extends AbstractHooksContainer
        implements ISuiteListener,
        ITestListener,
        IInvokedMethodListener,
        IExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);

    private final TestNgParameterExtractor testNgParameterExtractor;
    private final FacadeFunctionsHandler facadeFunctionsHandler;
    private final TestNgMethodExportManager methodExportManager;
    private final TestNgTestResultReporter reporter;
    private final Set<String> processedClasses;
    private final GlobalRunManager runManager;
    private final PropertyProvider provider;
    private final TestIdFilter testIdFilter;

    public TestNgListener() {
        this.facadeFunctionsHandler = new FacadeFunctionsHandler();
        this.testNgParameterExtractor = new TestNgParameterExtractor();
        this.methodExportManager = new TestNgMethodExportManager();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new TestNgTestResultReporter();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.testIdFilter = new TestIdFilter();
    }

    /**
     * Constructor for testing
     */
    public TestNgListener(TestNgMethodExportManager methodExportManager,
                          TestNgTestResultReporter reporter,
                          GlobalRunManager runManager,
                          PropertyProvider provider,
                          TestIdFilter testIdFilter,
                          TestNgParameterExtractor testNgParameterExtractor,
                          FacadeFunctionsHandler facadeFunctionsHandler) {
        this.runManager = runManager;
        this.reporter = reporter;
        this.methodExportManager = methodExportManager;
        this.provider = provider;
        this.testNgParameterExtractor = testNgParameterExtractor;
        this.facadeFunctionsHandler = facadeFunctionsHandler;
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.testIdFilter = testIdFilter;
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

        Set<Class<?>> testClasses = Arrays.stream(context.getAllTestMethods())
                .map(ITestNGMethod::getTestClass)
                .map(IClass::getRealClass)
                .collect(Collectors.toSet());

        for (Class<?> testClass : testClasses) {
            exportTestClassIfNotProcessed(testClass);
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
        if (method.isTestMethod()) {
            facadeFunctionsHandler.handleFacadeFunctions(method, testResult);
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

    private void exportTestClassIfNotProcessed(Class<?> testClass) {
        if (testClass == null) {
            return;
        }

        Class<?> topLevelClass = testClass;
        while (topLevelClass.getEnclosingClass() != null) {
            topLevelClass = topLevelClass.getEnclosingClass();
        }

        String className = topLevelClass.getName();
        if (processedClasses.add(className)) {
            log.debug("Exporting test class: {}", className);
            methodExportManager.loadTestBodyForClass(topLevelClass);
        } else {
            log.debug("Test class {} already processed", className);
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
