package io.reporter.testng.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.model.TestMetadata;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgTestWrapper;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

class TestNgMetaDataExtractorTest {

    private TestNgMetaDataExtractor extractor;

    @Mock
    private ITestResult mockTestResult;

    @Mock
    private ITestNGMethod mockTestNGMethod;

    @Mock
    private IClass mockTestClass;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        extractor = new TestNgMetaDataExtractor();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test
    @DisplayName("Should extract metadata from regular test with both @Title and @TestId annotations")
    void shouldExtractMetadataFromRegularTestWithBothAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("TEST-123", metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from regular test with only @Title annotation")
    void shouldExtractMetadataFromRegularTestWithOnlyTitle() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTitle");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Title Only Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from regular test with only @TestId annotation")
    void shouldExtractMetadataFromRegularTestWithOnlyTestId() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTestId");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("methodWithOnlyTestId", metadata.getTitle()); // Method name when no @Title
        assertEquals("TEST-456", metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from regular test without annotations")
    void shouldExtractMetadataFromRegularTestWithoutAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("methodWithoutAnnotations", metadata.getTitle()); // Method name
        assertNull(metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from disabled test with both annotations")
    void shouldExtractMetadataFromDisabledTestWithBothAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("TEST-123", metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from disabled test with only @Title annotation")
    void shouldExtractMetadataFromDisabledTestWithOnlyTitle() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTitle");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Title Only Test", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from disabled test without annotations")
    void shouldExtractMetadataFromDisabledTestWithoutAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("methodWithoutAnnotations", metadata.getTitle()); // Method name
        assertNull(metadata.getTestId());
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle regular test with complex class name")
    void shouldHandleRegularTestWithComplexClassName() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.integration.ComplexTestSuite");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("methodWithoutAnnotations", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("com.example.integration.ComplexTestSuite", metadata.getSuiteTitle());
        assertEquals("com.example.integration.ComplexTestSuite.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle disabled test with nested class")
    void shouldHandleDisabledTestWithNestedClass() throws NoSuchMethodException {
        // Given
        Method testMethod = NestedTestClass.class.getMethod("nestedTestMethod");
        Class<?> testClass = NestedTestClass.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("nestedTestMethod", metadata.getTitle());
        assertEquals("NESTED-001", metadata.getTestId());
        assertEquals("NestedTestClass", metadata.getSuiteTitle());
        assertEquals("NestedTestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty title annotation")
    void shouldHandleEmptyTitleAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTitle");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("", metadata.getTitle()); // Empty string from annotation
        assertEquals("EMPTY-TITLE", metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle empty testId annotation")
    void shouldHandleEmptyTestIdAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTestId");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Test With Empty ID", metadata.getTitle());
        assertEquals("", metadata.getTestId()); // Empty string from annotation
        assertEquals("TestMethods", metadata.getSuiteTitle());
        assertEquals("TestMethods.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle regular test with method that throws exception during reflection")
    void shouldHandleRegularTestMethod() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithSpecialCharacters");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Test with 特殊字符 and émojis 🎯", metadata.getTitle());
        assertEquals("SPECIAL-CHARS", metadata.getTestId());
        assertEquals("com.example.TestClass", metadata.getSuiteTitle());
        assertEquals("com.example.TestClass.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle package-less class for disabled test")
    void shouldHandlePackageLessClassForDisabledTest() throws NoSuchMethodException {
        // Given
        Method testMethod = SimpleTestClass.class.getMethod("simpleTest");
        Class<?> testClass = SimpleTestClass.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("simpleTest", metadata.getTitle());
        assertNull(metadata.getTestId());
        assertEquals("SimpleTestClass", metadata.getSuiteTitle());
        assertEquals("SimpleTestClass.java", metadata.getFile());
    }

    private TestNgTestWrapper setupRegularTestWrapper(Method method, String className) {
        // Setup method mock to return our test method
        when(mockTestNGMethod.getConstructorOrMethod()).thenReturn(
                new org.testng.internal.ConstructorOrMethod(method)
        );

        // Setup test class mock
        when(mockTestClass.getName()).thenReturn(className);

        // Setup test result mock
        when(mockTestResult.getMethod()).thenReturn(mockTestNGMethod);
        when(mockTestResult.getTestClass()).thenReturn(mockTestClass);

        return TestNgTestWrapper.forRegularTest(mockTestResult);
    }

    // Test helper classes
    private static class TestMethods {

        @Title("Custom Test Title")
        @TestId("TEST-123")
        public void methodWithBothAnnotations() {
        }

        @Title("Title Only Test")
        public void methodWithOnlyTitle() {
        }

        @TestId("TEST-456")
        public void methodWithOnlyTestId() {
        }

        public void methodWithoutAnnotations() {
        }

        @Title("")
        @TestId("EMPTY-TITLE")
        public void methodWithEmptyTitle() {
        }

        @Title("Test With Empty ID")
        @TestId("")
        public void methodWithEmptyTestId() {
        }

        @Title("Test with 特殊字符 and émojis 🎯")
        @TestId("SPECIAL-CHARS")
        public void methodWithSpecialCharacters() {
        }
    }

    private static class NestedTestClass {
        @TestId("NESTED-001")
        public void nestedTestMethod() {
        }
    }

    private static class SimpleTestClass {
        public void simpleTest() {
        }
    }
}