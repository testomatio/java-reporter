package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.testomat.core.annotation.TestId;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.xml.XmlSuite;

/**
 * TestNG listener for Testomat.io integration.
 * Reports TestNG test execution results to Testomat.io platform.
 * Supports custom annotations (@Title, @TestId) and handles disabled tests.
 * Also exports test method bodies when required.
 */
public class TestNgListener implements ISuiteListener, ITestListener, 
        IInvokedMethodListener, IAnnotationTransformer {
    private static final Logger log = LoggerFactory.getLogger(TestNgListener.class);
    private static final String LISTENING_REQUIRED_PROPERTY_NAME = "testomatio.listening";

    private final GlobalRunManager runManager;
    private final TestNgTestResultReporter reporter;
    private final TestNgMethodExportManager methodExportManager;
    private final PropertyProvider provider;

    private final Set<String> processedClasses;

    public TestNgListener() {
        this.methodExportManager = new TestNgMethodExportManager();
        this.processedClasses = ConcurrentHashMap.newKeySet();
        this.runManager = GlobalRunManager.getInstance();
        this.reporter = new TestNgTestResultReporter();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    /**
     * Constructor for testing
     */
    public TestNgListener(TestNgMethodExportManager methodExportManager,
                          TestNgTestResultReporter reporter,
                          GlobalRunManager runManager,
                          PropertyProvider provider) {
        this.runManager = runManager;
        this.reporter = reporter;
        this.methodExportManager = methodExportManager;
        this.provider = provider;
        this.processedClasses = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void onStart(ISuite suite) {
        if (!isListeningRequired()) {
            return;
        }
        log.debug("Suite started: {}", suite.getName());
        
        // Test ID filtering is handled by IAnnotationTransformer
        String providedIds = System.getProperty("ids");
        if (providedIds != null && !providedIds.trim().isEmpty()) {
            System.out.println("=== USING ANNOTATION TRANSFORMER FOR ID FILTER ===");
            System.out.println("Filter IDs: " + providedIds);
            System.out.println("Suite: " + suite.getName());
            
            try {
                XmlSuite xmlSuite = suite.getXmlSuite();
                System.out.println("XmlSuite: " + xmlSuite.getName());
                
                // Register this listener as annotation transformer
                xmlSuite.addListener(TestNgListener.class.getName());
                System.out.println("Registered TestNgListener as annotation transformer");
                System.out.println("Total listeners: " + xmlSuite.getListeners().size());
                
            } catch (Exception e) {
                System.out.println("Error registering annotation transformer: " + e.getMessage());
            }
        }
        
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
        
        String providedIds = System.getProperty("ids");
        if (providedIds != null && !providedIds.trim().isEmpty()) {
            
            System.out.println("=== TEST START FILTER DEBUG ===");
            System.out.println("Method: " + result.getTestClass().getName() 
                             + "." + result.getMethod().getMethodName());
            System.out.println("Provided IDs: '" + providedIds + "'");
            
            TestId testIdAnnotation = result.getMethod().getConstructorOrMethod()
                    .getMethod().getAnnotation(TestId.class);
            
            System.out.println("@TestId present: " + (testIdAnnotation != null));
            
            if (testIdAnnotation == null) {
                System.out.println("SKIPPING: No @TestId annotation");
                result.setStatus(ITestResult.SKIP);
                throw new org.testng.SkipException("Test filtered: no @TestId annotation");
            }
            
            String testId = testIdAnnotation.value();
            System.out.println("Found @TestId: '" + testId + "'");
            
            boolean shouldInclude = isTestIdIncluded(testId, providedIds);
            System.out.println("Should include: " + shouldInclude);
            
            if (!shouldInclude) {
                System.out.println("SKIPPING: Test ID not in filter");
                result.setStatus(ITestResult.SKIP);
                throw new org.testng.SkipException("Test filtered by ID: " + testId);
            }
            
            System.out.println("RUNNING: Test will execute");
            System.out.println("=== END TEST START FILTER DEBUG ===");
        }
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
        // Nothing to do after invocation for filtering
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, 
                         Constructor testConstructor, Method testMethod) {
        try {
            if (annotation == null || testMethod == null || testClass == null) {
                return;
            }
            
            String providedIds = System.getProperty("ids");
            if (providedIds == null || providedIds.trim().isEmpty()) {
                return; // No filtering
            }
            
            System.out.println("=== ANNOTATION TRANSFORMER DEBUG ===");
            System.out.println("Method: " + testClass.getName() + "." + testMethod.getName());
            System.out.println("Provided IDs: '" + providedIds + "'");
            
            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);
            System.out.println("@TestId present: " + (testIdAnnotation != null));
            
            if (testIdAnnotation == null) {
                System.out.println("DISABLING: No @TestId annotation");
                annotation.setEnabled(false);
                return;
            }
            
            String testId = testIdAnnotation.value();
            if (testId == null || testId.trim().isEmpty()) {
                System.out.println("DISABLING: Empty @TestId value");
                annotation.setEnabled(false);
                return;
            }
            
            System.out.println("Found @TestId: '" + testId + "'");
            
            boolean shouldInclude = isTestIdIncluded(testId, providedIds);
            System.out.println("Should include: " + shouldInclude);
            
            if (!shouldInclude) {
                System.out.println("DISABLING: Test ID not in filter");
                annotation.setEnabled(false);
            } else {
                System.out.println("KEEPING: Test will run");
            }
            
            System.out.println("=== END ANNOTATION TRANSFORMER DEBUG ===");
            
        } catch (Exception e) {
            System.out.println("ERROR in IAnnotationTransformer: " + e.getMessage());
            log.error("Error in IAnnotationTransformer for method: {}", 
                     testMethod != null ? testMethod.getName() : "unknown", e);
            // On error, don't disable the test - let it run
        }
    }
    
    private boolean isTestIdIncluded(String testId, String providedIds) {
        try {
            if (testId == null || providedIds == null) {
                return false;
            }
            
            String[] allowedIds = providedIds.split(",");
            for (String allowedId : allowedIds) {
                String trimmed = allowedId.trim();
                System.out.println("  Comparing: '" + testId + "' == '" + trimmed + "'");
                if (!trimmed.isEmpty() && testId.equals(trimmed)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("ERROR in isTestIdIncluded: " + e.getMessage());
            return false;
        }
    }

    private boolean isListeningRequired() {
        try {
            return provider.getProperty(LISTENING_REQUIRED_PROPERTY_NAME) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
