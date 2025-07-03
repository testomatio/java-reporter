package com.testomatio.reporter.core.frameworkintegration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.core.constructor.JUnitTestResultConstructor;
import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestResultWrapper;
import com.testomatio.reporter.core.extractor.JunitMetaDataExtractor;
import com.testomatio.reporter.core.extractor.wrapper.JUnitTestWrapper;
import com.testomatio.reporter.model.TestResult;
import com.testomatio.reporter.model.TestMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static com.testomatio.reporter.constants.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JunitExtensionTest {

    @Mock
    private ExtensionContext extensionContext;
    
    @Mock
    private GlobalRunManager globalRunManager;
    
    @Mock
    private JunitMetaDataExtractor metaDataExtractor;
    
    @Mock
    private TestMetadata testMetadata;
    
    @Mock
    private TestResult testResult;
    
    @Mock
    private ResultConstructor resultConstructor;
    
    @Mock
    private Throwable throwable;
    
    private JunitExtension junitExtension;
    private TestableJunitExtension testableExtension;
    private Method realTestMethod;
    private AutoCloseable mocks;
    
    private static class TestableJunitExtension extends JunitExtension {
        private boolean reportTestResultCalled = false;
        private TestMetadata lastMetadata;
        private String lastStatus;
        private String lastMessage;
        private Object lastFrameworkData;
        
        @Override
        protected void reportTestResult(TestMetadata metadata, String status, String message, Object frameworkSpecificData) {
            reportTestResultCalled = true;
            lastMetadata = metadata;
            lastStatus = status;
            lastMessage = message;
            lastFrameworkData = frameworkSpecificData;
        }
        
        public void resetCallTracking() {
            reportTestResultCalled = false;
            lastMetadata = null;
            lastStatus = null;
            lastMessage = null;
            lastFrameworkData = null;
        }
    }
    
    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        
        realTestMethod = this.getClass().getDeclaredMethod("dummyTestMethod");
        
        junitExtension = new JunitExtension();
        testableExtension = new TestableJunitExtension();
        
        injectDependencies(junitExtension);
        injectDependencies(testableExtension);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }
    
    private void dummyTestMethod() {
    }
    
    private void injectDependencies(JunitExtension extension) throws Exception {
        Field runManagerField = AbstractTestFrameworkListener.class.getDeclaredField("runManager");
        runManagerField.setAccessible(true);
        runManagerField.set(extension, globalRunManager);
        
        Field extractorField = JunitExtension.class.getDeclaredField("junitMetaDataExtractor");
        extractorField.setAccessible(true);
        extractorField.set(extension, metaDataExtractor);
        
        if (extension == junitExtension) {
            Field constructorField = AbstractTestFrameworkListener.class.getDeclaredField("resultConstructor");
            constructorField.setAccessible(true);
            constructorField.set(extension, resultConstructor);
        }
    }

    @Test
    void testCreateResultConstructor() {
        // Act
        ResultConstructor result = junitExtension.createResultConstructor();
        
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof JUnitTestResultConstructor);
    }

    @Test
    void testAddFrameworkSpecificData_WithExtensionContext() {
        // Arrange
        TestResultWrapper.Builder builder = mock(TestResultWrapper.Builder.class);
        
        // Act
        junitExtension.addFrameworkSpecificData(builder, extensionContext);
        
        // Assert
        verify(builder).withJunitExtensionContext(extensionContext);
    }

    @Test
    void testAddFrameworkSpecificData_WithNonExtensionContext() {
        // Arrange
        TestResultWrapper.Builder builder = mock(TestResultWrapper.Builder.class);
        Object nonExtensionContext = new Object();
        
        // Act
        junitExtension.addFrameworkSpecificData(builder, nonExtensionContext);
        
        // Assert
        verify(builder, never()).withJunitExtensionContext(any());
    }

    @Test
    void testAddFrameworkSpecificData_WithNull() {
        // Arrange
        TestResultWrapper.Builder builder = mock(TestResultWrapper.Builder.class);
        
        // Act
        junitExtension.addFrameworkSpecificData(builder, null);
        
        // Assert
        verify(builder, never()).withJunitExtensionContext(any());
    }

    @Test
    void testBeforeAll_WithTestClass() {
        // Arrange
        Class<?> testClass = String.class;
        when(extensionContext.getTestClass()).thenReturn(Optional.of(testClass));
        
        // Act
        junitExtension.beforeAll(extensionContext);
        
        // Assert
        verify(globalRunManager).incrementSuiteCounter();
    }

    @Test
    void testBeforeAll_WithoutTestClass() {
        // Arrange
        when(extensionContext.getTestClass()).thenReturn(Optional.empty());
        
        // Act
        junitExtension.beforeAll(extensionContext);
        
        // Assert
        verify(globalRunManager).incrementSuiteCounter();
    }

    @Test
    void testAfterAll_WithTestClass() {
        // Arrange
        Class<?> testClass = String.class;
        when(extensionContext.getTestClass()).thenReturn(Optional.of(testClass));
        
        // Act
        junitExtension.afterAll(extensionContext);
        
        // Assert
        verify(globalRunManager).decrementSuiteCounter();
    }

    @Test
    void testAfterAll_WithoutTestClass() {
        // Arrange
        when(extensionContext.getTestClass()).thenReturn(Optional.empty());
        
        // Act
        junitExtension.afterAll(extensionContext);
        
        // Assert
        verify(globalRunManager).decrementSuiteCounter();
    }

    @Test
    void testBeforeEach() {
        // Arrange
        String displayName = "testMethod";
        when(extensionContext.getDisplayName()).thenReturn(displayName);
        
        // Act
        junitExtension.beforeEach(extensionContext);
        
    }

    @Test
    void testTestDisabled_WithReason() {
        // Arrange
        setupMocksForTestExecution();
        String reason = "Test is disabled";
        
        // Act
        testableExtension.testDisabled(extensionContext, Optional.of(reason));
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(SKIPPED, testableExtension.lastStatus);
        assertEquals(reason, testableExtension.lastMessage);
        assertEquals(extensionContext, testableExtension.lastFrameworkData);
    }

    @Test
    void testTestDisabled_WithoutReason() {
        // Arrange
        setupMocksForTestExecution();
        
        // Act
        testableExtension.testDisabled(extensionContext, Optional.empty());
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(SKIPPED, testableExtension.lastStatus);
        assertEquals("Test disabled", testableExtension.lastMessage);
    }

    @Test
    void testTestSuccessful() {
        // Arrange
        setupMocksForTestExecution();
        
        // Act
        testableExtension.testSuccessful(extensionContext);
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(PASSED, testableExtension.lastStatus);
        assertNull(testableExtension.lastMessage);
    }

    @Test
    void testTestAborted() {
        // Arrange
        setupMocksForTestExecution();
        String message = "Test was aborted";
        when(throwable.getMessage()).thenReturn(message);
        
        // Act
        testableExtension.testAborted(extensionContext, throwable);
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(SKIPPED, testableExtension.lastStatus);
        assertEquals(message, testableExtension.lastMessage);
    }

    @Test
    void testTestFailed() {
        // Arrange
        setupMocksForTestExecution();
        String message = "Test failed";
        when(throwable.getMessage()).thenReturn(message);
        
        // Act
        testableExtension.testFailed(extensionContext, throwable);
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(FAILED, testableExtension.lastStatus);
        assertEquals(message, testableExtension.lastMessage);
    }

    @Test
    void testTestDisabled_NoTestMethod() {
        // Arrange
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        
        // Act
        testableExtension.testDisabled(extensionContext, Optional.of("Disabled"));
        
        // Assert
        assertFalse(testableExtension.reportTestResultCalled);
        verify(metaDataExtractor, never()).extractTestMetadata(any());
    }

    @Test
    void testTestSuccessful_NoTestMethod() {
        // Arrange
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        
        // Act
        testableExtension.testSuccessful(extensionContext);
        
        // Assert
        assertFalse(testableExtension.reportTestResultCalled);
        verify(metaDataExtractor, never()).extractTestMetadata(any());
    }

    @Test
    void testTestAborted_NoTestMethod() {
        // Arrange
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        
        // Act
        testableExtension.testAborted(extensionContext, throwable);
        
        // Assert
        assertFalse(testableExtension.reportTestResultCalled);
        verify(metaDataExtractor, never()).extractTestMetadata(any());
    }

    @Test
    void testTestFailed_NoTestMethod() {
        // Arrange
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        
        // Act
        testableExtension.testFailed(extensionContext, throwable);
        
        // Assert
        assertFalse(testableExtension.reportTestResultCalled);
        verify(metaDataExtractor, never()).extractTestMetadata(any());
    }

    @Test
    void testHandleTestResult_VerifyJUnitTestWrapperCreation() {
        // Arrange
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(realTestMethod));
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        when(metaDataExtractor.extractTestMetadata(any(JUnitTestWrapper.class))).thenReturn(testMetadata);
        ArgumentCaptor<JUnitTestWrapper> wrapperCaptor = ArgumentCaptor.forClass(JUnitTestWrapper.class);
        
        // Act
        testableExtension.testSuccessful(extensionContext);
        
        // Assert
        verify(metaDataExtractor).extractTestMetadata(wrapperCaptor.capture());
        JUnitTestWrapper capturedWrapper = wrapperCaptor.getValue();
        assertNotNull(capturedWrapper);
    }

    @Test
    void testTestAborted_NullCauseMessage() {
        // Arrange
        setupMocksForTestExecution();
        when(throwable.getMessage()).thenReturn(null);
        
        // Act
        testableExtension.testAborted(extensionContext, throwable);
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(SKIPPED, testableExtension.lastStatus);
        assertNull(testableExtension.lastMessage);
    }

    @Test
    void testTestFailed_NullCauseMessage() {
        // Arrange
        setupMocksForTestExecution();
        when(throwable.getMessage()).thenReturn(null);
        
        // Act
        testableExtension.testFailed(extensionContext, throwable);
        
        // Assert
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(FAILED, testableExtension.lastStatus);
        assertNull(testableExtension.lastMessage);
    }

    @Test
    void testMultipleTestExecutions() {
        // Arrange
        setupMocksForTestExecution();
        
        // Act & Assert - First test
        testableExtension.testSuccessful(extensionContext);
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(PASSED, testableExtension.lastStatus);
        
        // Reset and run second test
        testableExtension.resetCallTracking();
        testableExtension.testFailed(extensionContext, throwable);
        assertTrue(testableExtension.reportTestResultCalled);
        assertEquals(FAILED, testableExtension.lastStatus);
    }

    private void setupMocksForTestExecution() {
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(realTestMethod));
        when(extensionContext.getDisplayName()).thenReturn("testMethod");
        when(metaDataExtractor.extractTestMetadata(any(JUnitTestWrapper.class))).thenReturn(testMetadata);
        
        // Setup testMetadata mock
        when(testMetadata.getTitle()).thenReturn("Test Title");
        when(testMetadata.getTestId()).thenReturn("TEST-123");
        when(testMetadata.getSuiteTitle()).thenReturn("Suite Title");
        when(testMetadata.getFile()).thenReturn("TestFile.java");
    }
}