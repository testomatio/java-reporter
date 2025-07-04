package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.extractor.wrapper.TestNgTestWrapper;
import com.testomatio.reporter.model.TestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TestNGMetaDataExtractorTest {

    @Mock
    private TestNgTestWrapper testWrapper;

    @Mock
    private ITestResult testResult;

    @Mock
    private ITestNGMethod testNGMethod;

    @Mock
    private org.testng.IClass testClass;

    private TestNgMetaDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TestNgMetaDataExtractor();
    }

    @Test
    void extractTestMetadata_shouldCallRegularTestExtraction_whenIsRegularTestReturnsTrue() {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(true);
        when(testWrapper.getTestResult()).thenReturn(testResult);
        setupTestResultMocks("testMethod", "com.example.TestClass", null, null);

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertNotNull(result);
        assertEquals("testMethod", result.getTitle());
        assertEquals("com.example.TestClass", result.getSuiteTitle());
        assertEquals("com.example.TestClass.java", result.getFile());
        assertNull(result.getTestId());
    }

    @Test
    void extractTestMetadata_shouldCallDisabledTestExtraction_whenIsRegularTestReturnsFalse() throws Exception {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(false);
        Method method = TestClass.class.getDeclaredMethod("sampleTestMethod");
        when(testWrapper.getMethod()).thenReturn(method);
        doReturn(TestClass.class).when(testWrapper).getTestClass();

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertNotNull(result);
        assertEquals("sampleTestMethod", result.getTitle());
        assertEquals("TestClass", result.getSuiteTitle());
        assertEquals("TestClass.java", result.getFile());
        assertNull(result.getTestId());
    }

    @Test
    void extractTestMetadataForRegularTest_shouldExtractBasicMetadata() {
        // Given
        setupTestResultMocks("regularTest", "com.example.SampleTest", null, null);

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertEquals("regularTest", result.getTitle());
        assertEquals("com.example.SampleTest", result.getSuiteTitle());
        assertEquals("com.example.SampleTest.java", result.getFile());
        assertNull(result.getTestId());
    }

    @Test
    void extractTestMetadataForDisabledTest_shouldExtractBasicMetadata() throws Exception {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(false);
        Method method = TestClass.class.getDeclaredMethod("sampleTestMethod");
        when(testWrapper.getMethod()).thenReturn(method);
        doReturn(TestClass.class).when(testWrapper).getTestClass();

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertEquals("sampleTestMethod", result.getTitle());
        assertEquals("TestClass", result.getSuiteTitle());
        assertEquals("TestClass.java", result.getFile());
        assertNull(result.getTestId());
    }

    @Test
    void extractTestMetadataForDisabledTest_shouldUseAnnotationTitle_whenTitleAnnotationPresent() throws Exception {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(false);
        Method method = TestClassWithAnnotations.class.getDeclaredMethod("annotatedTestMethod");
        when(testWrapper.getMethod()).thenReturn(method);
        doReturn(TestClassWithAnnotations.class).when(testWrapper).getTestClass();

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertEquals("Custom Test Title", result.getTitle());
        assertEquals("TestClassWithAnnotations", result.getSuiteTitle());
        assertEquals("TestClassWithAnnotations.java", result.getFile());
        assertNull(result.getTestId());
    }

    @Test
    void extractTestMetadataForDisabledTest_shouldExtractTestId_whenTestIdAnnotationPresent() throws Exception {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(false);
        Method method = TestClassWithAnnotations.class.getDeclaredMethod("testMethodWithId");
        when(testWrapper.getMethod()).thenReturn(method);
        doReturn(TestClassWithAnnotations.class).when(testWrapper).getTestClass();

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertEquals("testMethodWithId", result.getTitle());
        assertEquals("TEST-789", result.getTestId());
        assertEquals("TestClassWithAnnotations", result.getSuiteTitle());
        assertEquals("TestClassWithAnnotations.java", result.getFile());
    }

    @Test
    void extractTestMetadataForDisabledTest_shouldExtractBothAnnotations_whenBothPresent() throws Exception {
        // Given
        when(testWrapper.isRegularTest()).thenReturn(false);
        Method method = TestClassWithAnnotations.class.getDeclaredMethod("fullyAnnotatedTestMethod");
        when(testWrapper.getMethod()).thenReturn(method);
        doReturn(TestClassWithAnnotations.class).when(testWrapper).getTestClass();

        // When
        TestMetadata result = extractor.extractTestMetadata(testWrapper);

        // Then
        assertEquals("Fully Annotated Test", result.getTitle());
        assertEquals("TEST-FULL", result.getTestId());
        assertEquals("TestClassWithAnnotations", result.getSuiteTitle());
        assertEquals("TestClassWithAnnotations.java", result.getFile());
    }

    private void setupTestResultMocks(String methodName, String className, String titleValue, String testIdValue) {
        when(testWrapper.isRegularTest()).thenReturn(true);
        when(testWrapper.getTestResult()).thenReturn(testResult);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getName()).thenReturn(methodName);
        when(testClass.getName()).thenReturn(className);

        try {
            Method method = createMockMethod(methodName, titleValue, testIdValue);
            when(testNGMethod.getConstructorOrMethod()).thenReturn(new org.testng.internal.ConstructorOrMethod(method));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Method createMockMethod(String methodName, String titleValue, String testIdValue) throws Exception {
        if (titleValue != null && testIdValue != null) {
            return TestClassWithAnnotations.class.getDeclaredMethod("fullyAnnotatedTestMethod");
        } else if (titleValue != null) {
            return TestClassWithAnnotations.class.getDeclaredMethod("annotatedTestMethod");
        } else if (testIdValue != null) {
            return TestClassWithAnnotations.class.getDeclaredMethod("testMethodWithId");
        } else {
            return TestClass.class.getDeclaredMethod("sampleTestMethod");
        }
    }

    static class TestClass {
        public void sampleTestMethod() {}
    }

    static class TestClassWithAnnotations {
        @Title("Custom Test Title")
        public void annotatedTestMethod() {}

        @TestId("TEST-789")
        public void testMethodWithId() {}

        @Title("Fully Annotated Test")
        @TestId("TEST-FULL")
        public void fullyAnnotatedTestMethod() {}
    }
}