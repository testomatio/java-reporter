package io.testomat.testng.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

import io.testomat.core.annotation.LinkTest;
import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import org.testng.ITestClass;
import org.testng.internal.ConstructorOrMethod;

class TestNgMetaDataExtractorTest {

    private TestNgMetaDataExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TestNgMetaDataExtractor();
    }

    @Test
    @DisplayName("Should extract metadata from regular test")
    void shouldExtractMetadataFromRegularTest() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithAnnotations");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testClass.getRealClass()).thenReturn((Class) TestClassForTesting.class);

        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("test-123", metadata.getTestId());
        assertEquals("TestClassForTesting", metadata.getSuiteTitle());
        assertEquals("io/testomat/testng/extractor/TestClassForTesting.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract metadata from disabled test")
    void shouldExtractMetadataFromDisabledTest() throws Exception {
        // Given
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithAnnotations");
        Class<?> testClass = TestClassForTesting.class;

        TestNgTestWrapper wrapper = new TestNgTestWrapper(method, testClass);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("Custom Test Title", metadata.getTitle());
        assertEquals("test-123", metadata.getTestId());
        assertEquals("TestClassForTesting", metadata.getSuiteTitle());
        assertEquals("io/testomat/testng/extractor/TestClassForTesting.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should use method name when @Title is not present")
    void shouldUseMethodNameWhenTitleNotPresent() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithoutTitle");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testClass.getRealClass()).thenReturn((Class) TestClassForTesting.class);

        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertNotNull(metadata);
        assertEquals("testMethodWithoutTitle", metadata.getTitle());
    }

    @Test
    @DisplayName("Should return null testId when @TestId is not present")
    void shouldReturnNullTestIdWhenAnnotationNotPresent() throws Exception {
        // Given
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithoutAnnotations");

        // When
        String testId = extractor.getTestId(method);

        // Then
        assertNull(testId);
    }

    @Test
    @DisplayName("Should extract testId from @TestId annotation")
    void shouldExtractTestIdFromAnnotation() throws Exception {
        // Given
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithAnnotations");

        // When
        String testId = extractor.getTestId(method);

        // Then
        assertEquals("test-123", testId);
    }

    @Test
    @DisplayName("Should build correct file path from class package")
    void shouldBuildCorrectFilePathFromPackage() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestClassForTesting.class.getDeclaredMethod("testMethodWithoutAnnotations");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testClass.getRealClass()).thenReturn((Class) TestClassForTesting.class);

        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertEquals("io/testomat/testng/extractor/TestClassForTesting.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should handle class without explicit package")
    void shouldHandleClassWithoutExplicitPackage() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = String.class.getMethod("toString");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testClass.getRealClass()).thenReturn((Class) String.class);

        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // When
        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        // Then
        assertEquals("String", metadata.getSuiteTitle());
        assertEquals("java/lang/String.java", metadata.getFile());
    }

    @Test
    @DisplayName("Should extract links from LinkTest annotation")
    void shouldExtractLinksFromAnnotation() throws Exception {
        Method method = TestClassForTesting.class
            .getDeclaredMethod("testMethodWithLinks");

        TestNgTestWrapper wrapper =
            new TestNgTestWrapper(method, TestClassForTesting.class);

        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        assertNotNull(metadata.getLinks());
        assertEquals(2, metadata.getLinks().size());

        assertEquals(
            "T-1",
            metadata.getLinks().get(0).getTest()
        );

        assertEquals(
            "T-2",
            metadata.getLinks().get(1).getTest()
        );
    }

    @Test
    @DisplayName("Should return empty links when LinkTest annotation absent")
    void shouldReturnEmptyLinksWhenNoAnnotation() throws Exception {
        Method method = TestClassForTesting.class
            .getDeclaredMethod("testMethodWithoutAnnotations");

        TestNgTestWrapper wrapper =
            new TestNgTestWrapper(method, TestClassForTesting.class);

        TestMetadata metadata = extractor.extractTestMetadata(wrapper);

        assertNotNull(metadata.getLinks());
        assertTrue(metadata.getLinks().isEmpty());
    }

    // Test class with annotations for testing
    static class TestClassForTesting {

        @Title("Custom Test Title")
        @TestId("test-123")
        public void testMethodWithAnnotations() {
        }

        @TestId("test-456")
        public void testMethodWithoutTitle() {
        }

        public void testMethodWithoutAnnotations() {
        }

        @LinkTest({"T-1", "T-2"})
        public void testMethodWithLinks() {
        }
    }
}
