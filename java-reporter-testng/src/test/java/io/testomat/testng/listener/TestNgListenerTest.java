package io.testomat.testng.listener;

import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.PropertyNameConstants.API_KEY_PROPERTY_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import io.testomat.testng.filter.TestIdFilter;
import io.testomat.testng.methodexporter.TestNgMethodExportManager;
import io.testomat.testng.reporter.TestNgTestResultReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

class TestNgListenerTest {

    private TestNgMethodExportManager methodExportManager;
    private TestNgListener listener;

    @BeforeEach
    void setUp() {
        methodExportManager = mock(TestNgMethodExportManager.class);
        TestNgTestResultReporter reporter = mock(TestNgTestResultReporter.class);
        PropertyProvider provider = mock(PropertyProvider.class);
        when(provider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("api-key");
        listener = new TestNgListener(methodExportManager,
                reporter,
                mock(GlobalRunManager.class),
                provider,
                mock(TestIdFilter.class),
                mock(TestNgParameterExtractor.class),
                mock(FacadeFunctionsHandler.class));
    }

    @Test
    @DisplayName("Should export top-level class for nested class test result")
    void shouldExportTopLevelClassForNestedClassTestResult() {
        ITestResult result = createTestResult(ExportTopLevelClass.NestedTestClassA.class);

        listener.onTestSuccess(result);

        verify(methodExportManager).loadTestBodyForClass(ExportTopLevelClass.class);
        verify(methodExportManager, never()).loadTestBodyForClass(ExportOtherTopLevelClass.class);
    }

    @Test
    @DisplayName("Should export top-level class only once for multiple nested test results")
    void shouldExportTopLevelClassOnlyOnceForMultipleNestedTestResults() {
        ITestResult firstResult = createTestResult(ExportTopLevelClass.NestedTestClassA.class);
        ITestResult secondResult = createTestResult(ExportTopLevelClass.NestedTestClassB.class);

        listener.onTestSuccess(firstResult);
        listener.onTestFailure(secondResult);

        verify(methodExportManager, times(1)).loadTestBodyForClass(ExportTopLevelClass.class);
    }

    @Test
    @DisplayName("Should not re-export already processed class on context finish")
    void shouldNotReExportAlreadyProcessedClassOnContextFinish() {
        ITestResult result = createTestResult(ExportTopLevelClass.NestedTestClassA.class);
        ITestNGMethod testMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);
        when(testMethod.getTestClass()).thenReturn(testClass);
        doReturn(ExportTopLevelClass.NestedTestClassB.class).when(testClass).getRealClass();
        ITestContext context = mock(ITestContext.class);
        when(context.getAllTestMethods()).thenReturn(new ITestNGMethod[]{testMethod});

        listener.onTestSuccess(result);
        listener.onFinish(context);

        verify(methodExportManager, times(1)).loadTestBodyForClass(ExportTopLevelClass.class);
    }

    @Test
    @DisplayName("Should export each top-level class at most once")
    void shouldExportEachTopLevelClassAtMostOnce() {
        ITestResult firstResult = createTestResult(ExportTopLevelClass.NestedTestClassA.class);
        ITestResult secondResult = createTestResult(ExportOtherTopLevelClass.NestedTestClass.class);

        listener.onTestSuccess(firstResult);
        listener.onTestSuccess(secondResult);

        verify(methodExportManager, times(1)).loadTestBodyForClass(ExportTopLevelClass.class);
        verify(methodExportManager, times(1))
                .loadTestBodyForClass(ExportOtherTopLevelClass.class);
    }

    @Test
    @DisplayName("Should not export anything when API key is not set")
    void shouldNotExportAnythingWhenApiKeyIsNotSet() {
        PropertyProvider provider = mock(PropertyProvider.class);
        when(provider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn(null);
        TestNgListener localListener = new TestNgListener(methodExportManager,
                mock(TestNgTestResultReporter.class),
                mock(GlobalRunManager.class),
                provider,
                mock(TestIdFilter.class),
                mock(TestNgParameterExtractor.class),
                mock(FacadeFunctionsHandler.class));
        ITestResult result = createTestResult(ExportTopLevelClass.NestedTestClassA.class);

        localListener.onTestSuccess(result);

        verify(methodExportManager, never()).loadTestBodyForClass(any());
    }

    @Test
    @DisplayName("Should skip export on skipped test events")
    void shouldSkipExportOnSkippedTestEvents() {
        ITestResult result = createTestResult(ExportTopLevelClass.NestedTestClassA.class);

        listener.onTestSkipped(result);

        verify(methodExportManager, times(1)).loadTestBodyForClass(ExportTopLevelClass.class);
    }

    @Test
    @DisplayName("Should report test result before exporting test class")
    void shouldReportTestResultBeforeExportingTestClass() {
        ITestResult result = createTestResult(ExportTopLevelClass.NestedTestClassA.class);
        TestNgTestResultReporter reporter = mock(TestNgTestResultReporter.class);
        PropertyProvider provider = mock(PropertyProvider.class);
        when(provider.getProperty(API_KEY_PROPERTY_NAME)).thenReturn("api-key");
        TestNgListener localListener = new TestNgListener(methodExportManager,
                reporter,
                mock(GlobalRunManager.class),
                provider,
                mock(TestIdFilter.class),
                mock(TestNgParameterExtractor.class),
                mock(FacadeFunctionsHandler.class));

        localListener.onTestSuccess(result);

        verify(reporter).reportTestResult(result, PASSED);
        verify(methodExportManager).loadTestBodyForClass(ExportTopLevelClass.class);
    }

    private ITestResult createTestResult(Class<?> realClass) {
        ITestResult result = mock(ITestResult.class);
        ITestClass testClass = mock(ITestClass.class);
        when(result.getTestClass()).thenReturn(testClass);
        doReturn(realClass).when(testClass).getRealClass();
        return result;
    }
}

class ExportTopLevelClass {
    static class NestedTestClassA {
    }

    static class NestedTestClassB {
    }
}

class ExportOtherTopLevelClass {
    static class NestedTestClass {
    }
}