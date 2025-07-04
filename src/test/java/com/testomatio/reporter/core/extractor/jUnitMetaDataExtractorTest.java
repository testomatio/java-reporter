package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.extractor.wrapper.JUnitTestWrapper;
import com.testomatio.reporter.model.TestMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class jUnitMetaDataExtractorTest {

    @Mock
    private JUnitTestWrapper wrapper;

    @Mock
    private ExtensionContext extensionContext;

    private JunitMetaDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new JunitMetaDataExtractor();
    }

    @Test
    void extractTestMetadata_WithTitleAndTestIdAnnotations_ShouldReturnCompleteMetadata() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithTitleAndTestId");
        String expectedTitle = "Custom Test Title";
        String expectedTestId = "TEST-123";
        String expectedSuiteTitle = "TestClass";
        String expectedFile = "TestClass.java";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getTestClass()).thenReturn(Optional.of(TestClass.class));

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedTitle, result.getTitle());
        assertEquals(expectedTestId, result.getTestId());
        assertEquals(expectedSuiteTitle, result.getSuiteTitle());
        assertEquals(expectedFile, result.getFile());
    }

    @Test
    void extractTestMetadata_WithoutTitleAnnotation_ShouldUseDisplayName() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithTestIdOnly");
        String expectedDisplayName = "testMethodDisplayName";
        String expectedTestId = "TEST-456";
        String expectedSuiteTitle = "TestClass";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getDisplayName()).thenReturn(expectedDisplayName);
        when(extensionContext.getTestClass()).thenReturn(Optional.of(TestClass.class));

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedDisplayName, result.getTitle());
        assertEquals(expectedTestId, result.getTestId());
        assertEquals(expectedSuiteTitle, result.getSuiteTitle());
    }

    @Test
    void extractTestMetadata_WithTitleWithoutTestId_ShouldReturnNullTestId() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithTitleOnly");
        String expectedTitle = "Test With Title Only";
        String expectedSuiteTitle = "TestClass";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getTestClass()).thenReturn(Optional.of(TestClass.class));

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedTitle, result.getTitle());
        assertNull(result.getTestId());
        assertEquals(expectedSuiteTitle, result.getSuiteTitle());
    }

    @Test
    void extractTestMetadata_WithoutAnnotations_ShouldUseDisplayNameAndNullTestId() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithoutAnnotations");
        String expectedDisplayName = "plainTestMethod";
        String expectedSuiteTitle = "TestClass";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getDisplayName()).thenReturn(expectedDisplayName);
        when(extensionContext.getTestClass()).thenReturn(Optional.of(TestClass.class));

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedDisplayName, result.getTitle());
        assertNull(result.getTestId());
        assertEquals(expectedSuiteTitle, result.getSuiteTitle());
    }

    @Test
    void extractTestMetadata_WithoutTestClass_ShouldReturnUnknownSuite() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithTitleOnly");
        String expectedTitle = "Test With Title Only";
        String expectedSuiteTitle = "Unknown";
        String expectedFile = "Unknown.java";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getTestClass()).thenReturn(Optional.empty());

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedTitle, result.getTitle());
        assertNull(result.getTestId());
        assertEquals(expectedSuiteTitle, result.getSuiteTitle());
        assertEquals(expectedFile, result.getFile());
    }

    @Test
    void extractTestMetadata_VerifyFileFormatting() throws NoSuchMethodException {
        // Given
        Method testMethod = TestClassWithAnnotations.class.getMethod("methodWithoutAnnotations");
        String expectedFile = "MyCustomClass.java";

        when(wrapper.getTestMethod()).thenReturn(testMethod);
        when(wrapper.getExtensionContext()).thenReturn(extensionContext);
        when(extensionContext.getDisplayName()).thenReturn("test");
        when(extensionContext.getTestClass()).thenReturn(Optional.of(MyCustomClass.class));

        // When
        TestMetadata result = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(result);
        assertEquals(expectedFile, result.getFile());
        assertEquals("MyCustomClass", result.getSuiteTitle());
    }

    @Test
    void getTestId_WithTestIdAnnotation_ShouldReturnTestId() throws NoSuchMethodException {
        // Given
        Method methodWithTestId = TestClassWithAnnotations.class.getMethod("methodWithTestIdOnly");

        // When
        String result = JunitMetaDataExtractor.getTestId(methodWithTestId);

        // Then
        assertEquals("TEST-456", result);
    }

    @Test
    void getTestId_WithoutTestIdAnnotation_ShouldReturnNull() throws NoSuchMethodException {
        // Given
        Method methodWithoutTestId = TestClassWithAnnotations.class.getMethod("methodWithoutAnnotations");

        // When
        String result = JunitMetaDataExtractor.getTestId(methodWithoutTestId);

        // Then
        assertNull(result);
    }

    private static class TestClass {
    }

    private static class MyCustomClass {
    }

    private static class TestClassWithAnnotations {
        @Title("Custom Test Title")
        @TestId("TEST-123")
        public void methodWithTitleAndTestId() {
        }

        @TestId("TEST-456")
        public void methodWithTestIdOnly() {
        }

        @Title("Test With Title Only")
        public void methodWithTitleOnly() {
        }

        public void methodWithoutAnnotations() {
        }
    }
}