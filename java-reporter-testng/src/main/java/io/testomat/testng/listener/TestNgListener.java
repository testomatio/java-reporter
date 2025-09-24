package io.testomat.testng.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.artifact.client.AwsService;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import io.testomat.testng.filter.TestIdFilter;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
public class TestNgListener implements ISuiteListener, ITestListener,
        IInvokedMethodListener {
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);
    private static final String LISTENING_REQUIRED_PROPERTY_NAME = "testomatio.listening";

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
    public void onStart(ISuite suite) {
        if (!isListeningRequired()) {
            return;
        }
        log.debug("Suite started: {}", suite.getName());
        runManager.incrementSuiteCounter();
        reporter.reportTestResult(suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (!isListeningRequired()) {
            return;
        }
        log.debug("Suite finished: {}", suite.getName());
        runManager.decrementSuiteCounter();
    }

    @Override
    public void onFinish(ITestContext context) {
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
    public void onTestSuccess(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        reporter.reportTestResult(result, PASSED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        reporter.reportTestResult(result, FAILED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }

        testIdFilter.filterTest(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (!isListeningRequired()) {
            return;
        }
        reporter.reportTestResult(result, SKIPPED);
        exportTestClassIfNotProcessed(result.getTestClass().getRealClass());
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

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod() && !defineArtifactsDisabled()) {
            awsService.uploadAllArtifactsForTest(testResult.getTestName(),
                    testNgParameterExtractor.generateRid(testResult),
                    metaDataExtractor.getTestId(
                            method.getTestMethod().getConstructorOrMethod().getMethod())
            );
        }
    }

    private boolean isListeningRequired() {
        try {
            return provider.getProperty(LISTENING_REQUIRED_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
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
}
