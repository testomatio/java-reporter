package io.reporter.testng.extractor;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.model.TestMetadata;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgTestWrapper;
import java.lang.reflect.Method;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestNgMetaDataExtractorTest {

    private TestNgMetaDataExtractor extractor;

    @Mock
    private ITestResult mockTestResult;

    @Mock
    private ITestNGMethod mockTestNGMethod;

    @Mock
    private IClass mockTestClass;

    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        extractor = new TestNgMetaDataExtractor();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    @Test(description = "Should extract metadata from regular test with both @Title and @TestId annotations")
    public void shouldExtractMetadataFromRegularTestWithBothAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Custom Test Title");
        assertEquals(metadata.getTestId(), "TEST-123");
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should extract metadata from regular test with only @Title annotation")
    public void shouldExtractMetadataFromRegularTestWithOnlyTitle() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTitle");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Title Only Test");
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should extract metadata from regular test with only @TestId annotation")
    public void shouldExtractMetadataFromRegularTestWithOnlyTestId() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTestId");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "methodWithOnlyTestId"); // Method name when no @Title
        assertEquals(metadata.getTestId(), "TEST-456");
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should extract metadata from regular test without annotations")
    public void shouldExtractMetadataFromRegularTestWithoutAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "methodWithoutAnnotations"); // Method name
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should extract metadata from disabled test with both annotations")
    public void shouldExtractMetadataFromDisabledTestWithBothAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithBothAnnotations");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Custom Test Title");
        assertEquals(metadata.getTestId(), "TEST-123");
        assertEquals(metadata.getSuiteTitle(), "TestMethods");
        assertEquals(metadata.getFile(), "TestMethods.java");
    }

    @Test(description = "Should extract metadata from disabled test with only @Title annotation")
    public void shouldExtractMetadataFromDisabledTestWithOnlyTitle() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithOnlyTitle");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Title Only Test");
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "TestMethods");
        assertEquals(metadata.getFile(), "TestMethods.java");
    }

    @Test(description = "Should extract metadata from disabled test without annotations")
    public void shouldExtractMetadataFromDisabledTestWithoutAnnotations() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "methodWithoutAnnotations"); // Method name
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "TestMethods");
        assertEquals(metadata.getFile(), "TestMethods.java");
    }

    @Test(description = "Should handle regular test with complex class name")
    public void shouldHandleRegularTestWithComplexClassName() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithoutAnnotations");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.integration.ComplexTestSuite");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "methodWithoutAnnotations");
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "com.example.integration.ComplexTestSuite");
        assertEquals(metadata.getFile(), "com.example.integration.ComplexTestSuite.java");
    }

    @Test(description = "Should handle disabled test with nested class")
    public void shouldHandleDisabledTestWithNestedClass() throws NoSuchMethodException {
        // Given
        Method testMethod = NestedTestClass.class.getMethod("nestedTestMethod");
        Class<?> testClass = NestedTestClass.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "nestedTestMethod");
        assertEquals(metadata.getTestId(), "NESTED-001");
        assertEquals(metadata.getSuiteTitle(), "NestedTestClass");
        assertEquals(metadata.getFile(), "NestedTestClass.java");
    }

    @Test(description = "Should handle empty title annotation")
    public void shouldHandleEmptyTitleAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTitle");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), ""); // Empty string from annotation
        assertEquals(metadata.getTestId(), "EMPTY-TITLE");
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should handle empty testId annotation")
    public void shouldHandleEmptyTestIdAnnotation() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithEmptyTestId");
        Class<?> testClass = TestMethods.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Test With Empty ID");
        assertEquals(metadata.getTestId(), ""); // Empty string from annotation
        assertEquals(metadata.getSuiteTitle(), "TestMethods");
        assertEquals(metadata.getFile(), "TestMethods.java");
    }

    @Test(description = "Should handle regular test with method that throws exception during reflection")
    public void shouldHandleRegularTestMethod() throws NoSuchMethodException {
        // Given
        Method testMethod = TestMethods.class.getMethod("methodWithSpecialCharacters");
        TestNgTestWrapper wrapper = setupRegularTestWrapper(testMethod, "com.example.TestClass");

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "Test with 特殊字符 and émojis 🎯");
        assertEquals(metadata.getTestId(), "SPECIAL-CHARS");
        assertEquals(metadata.getSuiteTitle(), "com.example.TestClass");
        assertEquals(metadata.getFile(), "com.example.TestClass.java");
    }

    @Test(description = "Should handle package-less class for disabled test")
    public void shouldHandlePackageLessClassForDisabledTest() throws NoSuchMethodException {
        // Given
        Method testMethod = SimpleTestClass.class.getMethod("simpleTest");
        Class<?> testClass = SimpleTestClass.class;
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(testMethod, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals(metadata.getTitle(), "simpleTest");
        assertNull(metadata.getTestId());
        assertEquals(metadata.getSuiteTitle(), "SimpleTestClass");
        assertEquals(metadata.getFile(), "SimpleTestClass.java");
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