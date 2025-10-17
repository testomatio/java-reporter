package io.testomat.testng.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.core.annotation.TestId;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import org.testng.SkipException;
import org.testng.internal.ConstructorOrMethod;

class TestIdFilterTest {

    private TestIdFilter filter;
    private String originalIdsProperty;

    @BeforeEach
    void setUp() {
        filter = new TestIdFilter();
        originalIdsProperty = System.getProperty("ids");
    }

    @AfterEach
    void tearDown() {
        if (originalIdsProperty != null) {
            System.setProperty("ids", originalIdsProperty);
        } else {
            System.clearProperty("ids");
        }
    }

    @Test
    @DisplayName("Should not filter when ids property is not set")
    void shouldNotFilterWhenIdsPropertyNotSet() throws Exception {
        // Given
        System.clearProperty("ids");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should not filter when ids property is empty")
    void shouldNotFilterWhenIdsPropertyIsEmpty() throws Exception {
        // Given
        System.setProperty("ids", "");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should not filter when ids property is blank")
    void shouldNotFilterWhenIdsPropertyIsBlank() throws Exception {
        // Given
        System.setProperty("ids", "   ");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should throw SkipException when test has no @TestId annotation")
    void shouldThrowSkipExceptionWhenNoTestIdAnnotation() throws Exception {
        // Given
        System.setProperty("ids", "test-123");
        ITestResult testResult = createTestResultWithoutTestId();

        // When & Then
        SkipException exception = assertThrows(SkipException.class,
                () -> filter.filterTest(testResult));
        assertTrue(exception.getMessage().contains("no @TestId annotation"));
    }

    @Test
    @DisplayName("Should not filter when test ID matches filter")
    void shouldNotFilterWhenTestIdMatches() throws Exception {
        // Given
        System.setProperty("ids", "test-123");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should throw SkipException when test ID does not match filter")
    void shouldThrowSkipExceptionWhenTestIdDoesNotMatch() throws Exception {
        // Given
        System.setProperty("ids", "test-456");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        SkipException exception = assertThrows(SkipException.class,
                () -> filter.filterTest(testResult));
        assertTrue(exception.getMessage().contains("test-123"));
    }

    @Test
    @DisplayName("Should not filter when test ID matches one of multiple filter IDs")
    void shouldNotFilterWhenTestIdMatchesOneOfMultiple() throws Exception {
        // Given
        System.setProperty("ids", "test-123,test-456,test-789");
        ITestResult testResult = createTestResult("test-456");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should handle filter IDs with spaces")
    void shouldHandleFilterIdsWithSpaces() throws Exception {
        // Given
        System.setProperty("ids", " test-123 , test-456 , test-789 ");
        ITestResult testResult = createTestResult("test-456");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should throw SkipException when test ID matches none of multiple filter IDs")
    void shouldThrowSkipExceptionWhenTestIdMatchesNone() throws Exception {
        // Given
        System.setProperty("ids", "test-123,test-456");
        ITestResult testResult = createTestResult("test-999");

        // When & Then
        SkipException exception = assertThrows(SkipException.class,
                () -> filter.filterTest(testResult));
        assertTrue(exception.getMessage().contains("test-999"));
    }

    @Test
    @DisplayName("Should handle single ID in filter list")
    void shouldHandleSingleIdInFilterList() throws Exception {
        // Given
        System.setProperty("ids", "test-single");
        ITestResult testResult = createTestResult("test-single");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should handle comma-separated list with empty values")
    void shouldHandleCommaListWithEmptyValues() throws Exception {
        // Given
        System.setProperty("ids", "test-123,,test-456,");
        ITestResult testResult = createTestResult("test-456");

        // When & Then
        assertDoesNotThrow(() -> filter.filterTest(testResult));
    }

    @Test
    @DisplayName("Should match exact ID only")
    void shouldMatchExactIdOnly() throws Exception {
        // Given
        System.setProperty("ids", "test-12");
        ITestResult testResult = createTestResult("test-123");

        // When & Then
        SkipException exception = assertThrows(SkipException.class,
                () -> filter.filterTest(testResult));
        assertTrue(exception.getMessage().contains("test-123"));
    }

    private ITestResult createTestResult(String testId) throws Exception {
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestClassWithId.class.getDeclaredMethod("testMethodWithId");

        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        TestId spyAnnotation = mock(TestId.class);
        when(spyAnnotation.value()).thenReturn(testId);

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        Method mockMethod = mock(Method.class);
        when(mockMethod.getAnnotation(TestId.class)).thenReturn(spyAnnotation);
        when(constructorOrMethod.getMethod()).thenReturn(mockMethod);

        return testResult;
    }

    private ITestResult createTestResultWithoutTestId() throws Exception {
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestClassWithId.class.getDeclaredMethod("testMethodWithoutId");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        return testResult;
    }

    static class TestClassWithId {

        @TestId("test-123")
        public void testMethodWithId() {
        }

        public void testMethodWithoutId() {
        }
    }
}
