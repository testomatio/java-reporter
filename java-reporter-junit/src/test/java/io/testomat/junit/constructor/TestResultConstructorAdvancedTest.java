package io.testomat.junit.constructor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.TestAbortedException;

/**
 * Advanced production-level test suite for JUnitTestResultConstructor.
 * Covers constructor validation, parameterized test support, error handling,
 * edge cases, and production scenarios including concurrency and memory stress.
 */
@DisplayName("TestResultConstructor Advanced Tests")
class TestResultConstructorAdvancedTest {

    private JUnitTestResultConstructor constructor;
    private TestMetadata testMetadata;
    private ExtensionContext mockContext;
    private JunitMetaDataExtractor mockExtractor;

    @BeforeEach
    void setUp() {
        constructor = new JUnitTestResultConstructor();
        mockExtractor = mock(JunitMetaDataExtractor.class);
        testMetadata = new TestMetadata(
                "Test Method Name",
                "TEST-001",
                "Test Suite Name",
                "TestClass.java"
        );
        mockContext = mock(ExtensionContext.class);
    }

    @Nested
    @DisplayName("Constructor Validation Tests")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Should create constructor with default extractor")
        void shouldCreateConstructorWithDefaultExtractor() {
            // When
            JUnitTestResultConstructor defaultConstructor = new JUnitTestResultConstructor();

            // Then
            assertNotNull(defaultConstructor);
        }

        @Test
        @DisplayName("Should create constructor with provided extractor")
        void shouldCreateConstructorWithProvidedExtractor() {
            // Given
            JunitMetaDataExtractor customExtractor = mock(JunitMetaDataExtractor.class);

            // When
            JUnitTestResultConstructor customConstructor = new JUnitTestResultConstructor(customExtractor);

            // Then
            assertNotNull(customConstructor);
        }

        @Test
        @DisplayName("Should handle null extractor in constructor")
        void shouldHandleNullExtractorInConstructor() {
            // When - current implementation doesn't validate null extractor
            JUnitTestResultConstructor constructorWithNull = new JUnitTestResultConstructor(null);

            // Then - constructor accepts null but may fail later
            assertNotNull(constructorWithNull);
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should validate null metadata")
        void shouldValidateNullMetadata() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> constructor.constructTestRunResult(null, "message", PASSED, mockContext));
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should handle null status gracefully")
        void shouldHandleNullStatus() {
            // Given
            String customMessage = "Test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, null, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(customMessage, result.getMessage());
            assertNull(result.getStatus());
        }

        @Test
        @DisplayName("Should handle empty status")
        void shouldHandleEmptyStatus() {
            // Given
            String customMessage = "Test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, "", mockContext);

            // Then
            assertNotNull(result);
            assertEquals(customMessage, result.getMessage());
            assertEquals("", result.getStatus());
        }

        @Test
        @DisplayName("Should handle whitespace-only status")
        void shouldHandleWhitespaceOnlyStatus() {
            // Given
            String customMessage = "Test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, "   ", mockContext);

            // Then
            assertNotNull(result);
            assertEquals(customMessage, result.getMessage());
            assertEquals("   ", result.getStatus());
        }

        @Test
        @DisplayName("Should handle null context")
        void shouldHandleNullContext() {
            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, "message", PASSED, null);

            // Then
            assertNotNull(result);
            assertEquals("message", result.getMessage());
            assertEquals(PASSED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("Parameterized Test Support")
    class ParameterizedTestSupportTests {

        @Test
        @DisplayName("Should handle parameterized test with single parameter")
        void shouldHandleParameterizedTestWithSingleParameter() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
            when(mockExtractor.extractTestParameters(mockContext)).thenReturn("test-value");
            when(mockContext.getUniqueId()).thenReturn("unique-test-id-123");
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Test message", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals("test-value", result.getExample());
            assertEquals("unique-test-id-123", result.getRid());
            verify(mockExtractor).isParameterizedTest(mockContext);
            verify(mockExtractor).extractTestParameters(mockContext);
        }

        @Test
        @DisplayName("Should handle parameterized test with multiple parameters")
        void shouldHandleParameterizedTestWithMultipleParameters() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("param1", "value1");
            parameters.put("param2", 42);
            parameters.put("param3", true);
            
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
            when(mockExtractor.extractTestParameters(mockContext)).thenReturn(parameters);
            when(mockContext.getUniqueId()).thenReturn("multi-param-test-id");
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Multi-param test", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(parameters, result.getExample());
            assertEquals("multi-param-test-id", result.getRid());
        }

        @Test
        @DisplayName("Should handle parameterized test with null parameters")
        void shouldHandleParameterizedTestWithNullParameters() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
            when(mockExtractor.extractTestParameters(mockContext)).thenReturn(null);
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Test message", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertNull(result.getExample());
            assertNull(result.getRid());
        }

        @Test
        @DisplayName("Should handle parameterized test with extraction exception")
        void shouldHandleParameterizedTestWithExtractionException() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
            when(mockExtractor.extractTestParameters(mockContext)).thenThrow(new RuntimeException("Extraction failed"));
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When & Then - current implementation doesn't catch extraction exceptions
            assertThrows(RuntimeException.class, () -> 
                constructorWithMock.constructTestRunResult(testMetadata, "Test message", PASSED, mockContext));
        }

        @Test
        @DisplayName("Should not add example/rid for regular tests")
        void shouldNotAddExampleRidForRegularTests() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(false);
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Regular test", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertNull(result.getExample());
            assertNull(result.getRid());
            verify(mockExtractor, times(0)).extractTestParameters(any());
        }

        @Test
        @DisplayName("Should handle parameterized test with complex objects")
        void shouldHandleParameterizedTestWithComplexObjects() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);
            TestData complexData = new TestData("test", 123, true);
            
            when(mockExtractor.isParameterizedTest(mockContext)).thenReturn(true);
            when(mockExtractor.extractTestParameters(mockContext)).thenReturn(complexData);
            when(mockContext.getUniqueId()).thenReturn("complex-object-test");
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Complex object test", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(complexData, result.getExample());
            assertEquals("complex-object-test", result.getRid());
        }

        @Test
        @DisplayName("Should handle null context for parameterized test")
        void shouldHandleNullContextForParameterizedTest() {
            // Given
            JUnitTestResultConstructor constructorWithMock = new JUnitTestResultConstructor(mockExtractor);

            // When
            TestResult result = constructorWithMock.constructTestRunResult(
                    testMetadata, "Test with null context", PASSED, null);

            // Then
            assertNotNull(result);
            assertNull(result.getExample());
            assertNull(result.getRid());
            // Current implementation calls isParameterizedTest even with null context
            verify(mockExtractor, times(1)).isParameterizedTest(null);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should extract exception message and stack when message is null")
        void shouldExtractExceptionDetailsWhenMessageIsNull() {
            // Given
            RuntimeException exception = new RuntimeException("Test failed with exception");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertEquals("Test failed with exception", result.getMessage());
            assertEquals(FAILED, result.getStatus());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("RuntimeException"));
            assertTrue(result.getStack().contains("Test failed with exception"));
        }

        @Test
        @DisplayName("Should filter TestAbortedException")
        void shouldFilterTestAbortedException() {
            // Given
            TestAbortedException abortedException = new TestAbortedException("Test was aborted");
            when(mockContext.getExecutionException()).thenReturn(Optional.of(abortedException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, SKIPPED, mockContext);

            // Then
            assertNull(result.getMessage());
            assertEquals(SKIPPED, result.getStatus());
            assertNull(result.getStack());
        }

        @Test
        @DisplayName("Should handle exception with null message")
        void shouldHandleExceptionWithNullMessage() {
            // Given
            RuntimeException exception = new RuntimeException((String) null);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertNull(result.getMessage());
            assertEquals(FAILED, result.getStatus());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("RuntimeException"));
        }

        @Test
        @DisplayName("Should handle nested exceptions correctly")
        void shouldHandleNestedExceptions() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException wrappedException = new IllegalStateException("Wrapper exception", rootCause);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(wrappedException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertEquals("Wrapper exception", result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("IllegalStateException"));
            assertTrue(result.getStack().contains("Wrapper exception"));
            assertTrue(result.getStack().contains("Caused by"));
            assertTrue(result.getStack().contains("Root cause"));
        }
    }

    @Nested
    @DisplayName("Production Edge Cases")
    class ProductionEdgeCasesTests {

        @Test
        @DisplayName("Should handle concurrent test execution")
        void shouldHandleConcurrentTestExecution() throws InterruptedException {
            // Given
            int numThreads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);
            TestResult[] results = new TestResult[numThreads];

            // When
            for (int i = 0; i < numThreads; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        TestMetadata threadMetadata = new TestMetadata(
                                "Thread Test " + threadIndex,
                                "THREAD-" + threadIndex,
                                "Thread Suite",
                                "ThreadTest.java"
                        );
                        results[threadIndex] = constructor.constructTestRunResult(
                                threadMetadata, "Concurrent test " + threadIndex, PASSED, null);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all threads to complete
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();

            // Then
            for (int i = 0; i < numThreads; i++) {
                assertNotNull(results[i]);
                assertEquals("Thread Test " + i, results[i].getTitle());
                assertEquals("THREAD-" + i, results[i].getTestId());
                assertEquals("Concurrent test " + i, results[i].getMessage());
            }
        }

        @Test
        @DisplayName("Should handle memory stress conditions")
        void shouldHandleMemoryStressConditions() {
            // Given
            String largeMessage = "X".repeat(100000); // 100KB message
            RuntimeException largeException = new RuntimeException(largeMessage);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(largeException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(largeMessage, result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains(largeMessage));
        }

        @Test
        @DisplayName("Should handle large test metadata")
        void shouldHandleLargeTestMetadata() {
            // Given
            TestMetadata largeMetadata = new TestMetadata(
                    "A".repeat(1000), // Very long test title
                    "ID-" + "X".repeat(500), // Very long test ID
                    "Suite " + "Y".repeat(800), // Very long suite title
                    "com/very/deep/package/structure/".repeat(10) + "TestClass.java"
            );
            String customMessage = "Z".repeat(2000); // Large message

            // When
            TestResult result = constructor.constructTestRunResult(
                    largeMetadata, customMessage, PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals(largeMetadata.getTitle(), result.getTitle());
            assertEquals(largeMetadata.getTestId(), result.getTestId());
            assertEquals(customMessage, result.getMessage());
            assertEquals(PASSED, result.getStatus());
        }

        @Test
        @DisplayName("Should handle special characters in all fields")
        void shouldHandleSpecialCharactersInAllFields() {
            // Given
            String specialChars = "Test with: \n\t\r\"Special chars: áéíóú ñ üöä 中文 🚀\"";
            TestMetadata specialMetadata = new TestMetadata(
                    specialChars,
                    "ID-" + specialChars,
                    "Suite-" + specialChars,
                    "path/" + specialChars + ".java"
            );
            RuntimeException exception = new RuntimeException(specialChars);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(exception));

            // When
            TestResult result = constructor.constructTestRunResult(
                    specialMetadata, null, FAILED, mockContext);

            // Then
            assertEquals(specialChars, result.getTitle());
            assertEquals("ID-" + specialChars, result.getTestId());
            assertEquals("Suite-" + specialChars, result.getSuiteTitle());
            assertEquals(specialChars, result.getMessage());
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains(specialChars));
        }

        @Test
        @DisplayName("Should preserve metadata with null fields")
        void shouldPreserveMetadataWithNullFields() {
            // Given
            TestMetadata nullFieldsMetadata = new TestMetadata(null, null, null, null);
            String customMessage = "Test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    nullFieldsMetadata, customMessage, PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertNull(result.getTitle());
            assertNull(result.getTestId());
            assertNull(result.getSuiteTitle());
            assertNull(result.getFile());
            assertEquals(customMessage, result.getMessage());
            assertEquals(PASSED, result.getStatus());
        }

        @Test
        @DisplayName("Should handle stack trace generation with deep call stack")
        void shouldHandleStackTraceGenerationWithDeepCallStack() {
            // Given
            RuntimeException deepException = createDeepStackTraceException(20);
            when(mockContext.getExecutionException()).thenReturn(Optional.of(deepException));

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, null, FAILED, mockContext);

            // Then
            assertNotNull(result.getStack());
            assertTrue(result.getStack().contains("createDeepStackTraceException"));
            assertTrue(result.getStack().contains("RuntimeException"));
            // Should contain multiple "at " entries for the deep stack
            long atCount = result.getStack().chars().filter(ch -> ch == '\n').count();
            assertTrue(atCount > 10); // Deep stack should have many entries
        }

        private RuntimeException createDeepStackTraceException(int depth) {
            if (depth <= 0) {
                return new RuntimeException("Deep stack trace exception");
            }
            return createDeepStackTraceException(depth - 1);
        }
    }

    @Nested
    @DisplayName("Integration and Regression Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should maintain backward compatibility with existing API")
        void shouldMaintainBackwardCompatibility() {
            // This test ensures that the refactored constructor maintains
            // the same public API and behavior as the original implementation

            // Given - using the original test pattern
            String customMessage = "Custom test message";

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, customMessage, PASSED, mockContext);

            // Then - verify all expected fields are present and correct
            assertNotNull(result);
            assertEquals(testMetadata.getTitle(), result.getTitle());
            assertEquals(testMetadata.getTestId(), result.getTestId());
            assertEquals(testMetadata.getSuiteTitle(), result.getSuiteTitle());
            assertEquals(testMetadata.getFile(), result.getFile());
            assertEquals(customMessage, result.getMessage());
            assertEquals(PASSED, result.getStatus());
        }

        @Test
        @DisplayName("Should work with real JUnit extension context pattern")
        void shouldWorkWithRealJUnitExtensionContextPattern() {
            // Given - simulating real JUnit context behavior
            when(mockContext.getExecutionException()).thenReturn(Optional.empty());
            when(mockContext.getUniqueId()).thenReturn("real-junit-id");

            // When
            TestResult result = constructor.constructTestRunResult(
                    testMetadata, "Integration test", PASSED, mockContext);

            // Then
            assertNotNull(result);
            assertEquals("Integration test", result.getMessage());
            assertEquals(PASSED, result.getStatus());
            assertNull(result.getStack()); // No exception, so no stack
        }
    }

    /**
     * Test data class for complex parameter testing
     */
    private static class TestData {
        private final String name;
        private final int value;
        private final boolean flag;

        public TestData(String name, int value, boolean flag) {
            this.name = name;
            this.value = value;
            this.flag = flag;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestData testData = (TestData) obj;
            return value == testData.value &&
                   flag == testData.flag &&
                   java.util.Objects.equals(name, testData.name);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, value, flag);
        }

        @Override
        public String toString() {
            return "TestData{name='" + name + "', value=" + value + ", flag=" + flag + "}";
        }
    }
}