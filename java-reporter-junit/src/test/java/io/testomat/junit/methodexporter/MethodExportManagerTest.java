package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.javaparser.ast.CompilationUnit;
import io.testomat.core.exception.PropertyNotFoundException;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.exception.ExtractionException;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.extractors.MethodCaseExtractor;
import io.testomat.junit.methodexporter.parser.FileParser;
import io.testomat.junit.methodexporter.patfinder.FileFinder;
import io.testomat.junit.methodexporter.sender.ExportSender;
import io.testomat.junit.model.ExporterTestCase;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("MethodExportManager Tests")
class MethodExportManagerTest {

    @Mock
    private PropertyProvider mockPropertyProvider;

    @Mock
    private FileFinder mockFileFinder;

    @Mock
    private ExportSender mockExportSender;

    @Mock
    private FileParser mockFileParser;

    @Mock
    private MethodCaseExtractor mockMethodCaseExtractor;

    @Mock
    private ExtensionContext mockExtensionContext;

    @Mock
    private CompilationUnit mockCompilationUnit;

    private MethodExportManager methodExportManager;

    // Test classes for testing
    public static class TestClassA {}
    public static class TestClassB {}

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Clear static processedClasses map before each test
        clearProcessedClasses();

        methodExportManager = new MethodExportManager(
                mockPropertyProvider,
                mockFileFinder,
                mockExportSender,
                mockFileParser,
                mockMethodCaseExtractor
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clear static processedClasses map after each test
        clearProcessedClasses();
    }

    private void clearProcessedClasses() throws Exception {
        // Use reflection to clear the static ConcurrentHashMap
        Field field = MethodExportManager.class.getDeclaredField("processedClasses");
        field.setAccessible(true);
        ConcurrentHashMap<String, Boolean> processedClasses =
                (ConcurrentHashMap<String, Boolean>) field.get(null);
        processedClasses.clear();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void shouldCreateInstanceWithDefaultConstructor() {
            // When
            MethodExportManager manager = new MethodExportManager();

            // Then
            assertNotNull(manager);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void shouldCreateInstanceWithParameterizedConstructor() {
            // When
            MethodExportManager manager = new MethodExportManager(
                    mockPropertyProvider, mockFileFinder, mockExportSender,
                    mockFileParser, mockMethodCaseExtractor
            );

            // Then
            assertNotNull(manager);
        }
    }

    @Nested
    @DisplayName("loadTestBodyForClass Tests")
    class LoadTestBodyForClassTests {

        @Test
        @DisplayName("Should return early when export is not required")
        void shouldReturnEarlyWhenExportIsNotRequired() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenThrow(new PropertyNotFoundException("Property not found"));

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockFileFinder, never()).getTestClassFilePath(any());
            verify(mockFileParser, never()).parseFile(anyString());
            verify(mockMethodCaseExtractor, never()).extractTestCases(any(), anyString());
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should return early when class already processed")
        void shouldReturnEarlyWhenClassAlreadyProcessed() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");

            // When - process same class twice
            methodExportManager.loadTestBodyForClass(TestClassA.class);
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then - should only process once
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
        }

        @Test
        @DisplayName("Should return early when FileFinder returns null")
        void shouldReturnEarlyWhenFileFinderReturnsNull() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(null);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileParser, never()).parseFile(anyString());
            verify(mockMethodCaseExtractor, never()).extractTestCases(any(), anyString());
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should return early when FileParser returns null")
        void shouldReturnEarlyWhenFileParserReturnsNull() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(null);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockFileParser, times(1)).parseFile(filepath);
            verify(mockMethodCaseExtractor, never()).extractTestCases(any(), anyString());
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should handle FileParser exception gracefully")
        void shouldHandleFileParserExceptionGracefully() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath))
                    .thenThrow(new MethodExporterException("Parse error"));

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockFileParser, times(1)).parseFile(filepath);
            verify(mockMethodCaseExtractor, never()).extractTestCases(any(), anyString());
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should return early when MethodCaseExtractor returns null")
        void shouldReturnEarlyWhenMethodCaseExtractorReturnsNull() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(null);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockMethodCaseExtractor, times(1)).extractTestCases(mockCompilationUnit, filepath);
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should return early when MethodCaseExtractor returns empty list")
        void shouldReturnEarlyWhenMethodCaseExtractorReturnsEmptyList() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            List<ExporterTestCase> emptyTestCases = Collections.emptyList();

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(emptyTestCases);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockMethodCaseExtractor, times(1)).extractTestCases(mockCompilationUnit, filepath);
            verify(mockExportSender, never()).sendTestCases(any());
        }

        @Test
        @DisplayName("Should handle ExportSender exception gracefully")
        void shouldHandleExportSenderExceptionGracefully() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            List<ExporterTestCase> testCases = createTestCases(2);

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(testCases);
            doThrow(new RuntimeException("Send failed")).when(mockExportSender).sendTestCases(testCases);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockExportSender, times(1)).sendTestCases(testCases);
        }

        @Test
        @DisplayName("Should successfully process test class with test cases")
        void shouldSuccessfullyProcessTestClassWithTestCases() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            List<ExporterTestCase> testCases = createTestCases(3);

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(testCases);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileParser, times(1)).parseFile(filepath);
            verify(mockMethodCaseExtractor, times(1)).extractTestCases(mockCompilationUnit, filepath);
            verify(mockExportSender, times(1)).sendTestCases(testCases);
        }

        @Test
        @DisplayName("Should process different classes separately")
        void shouldProcessDifferentClassesSeparately() {
            // Given
            String filepathA = "src/test/java/TestClassA.java";
            String filepathB = "src/test/java/TestClassB.java";
            List<ExporterTestCase> testCasesA = createTestCases(2);
            List<ExporterTestCase> testCasesB = createTestCases(3);

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepathA);
            when(mockFileFinder.getTestClassFilePath(TestClassB.class)).thenReturn(filepathB);
            when(mockFileParser.parseFile(filepathA)).thenReturn(mockCompilationUnit);
            when(mockFileParser.parseFile(filepathB)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepathA))
                    .thenReturn(testCasesA);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepathB))
                    .thenReturn(testCasesB);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);
            methodExportManager.loadTestBodyForClass(TestClassB.class);

            // Then
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassB.class);
            verify(mockExportSender, times(1)).sendTestCases(testCasesA);
            verify(mockExportSender, times(1)).sendTestCases(testCasesB);
        }
    }

    @Nested
    @DisplayName("loadTestBodyIfRequired Tests")
    class LoadTestBodyIfRequiredTests {

        @Test
        @DisplayName("Should return early when export is not required")
        void shouldReturnEarlyWhenExportIsNotRequired() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenThrow(new PropertyNotFoundException("Property not found"));

            // When
            methodExportManager.loadTestBodyIfRequired(mockExtensionContext);

            // Then
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockExtensionContext, never()).getRequiredTestClass();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when extensionContext is null")
        void shouldThrowIllegalArgumentExceptionWhenExtensionContextIsNull() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> methodExportManager.loadTestBodyIfRequired(null)
            );

            assertEquals("extensionContext is null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return early when class already processed")
        void shouldReturnEarlyWhenClassAlreadyProcessed() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            doReturn(TestClassA.class).when(mockExtensionContext).getRequiredTestClass();

            // When - process same context twice
            methodExportManager.loadTestBodyIfRequired(mockExtensionContext);
            methodExportManager.loadTestBodyIfRequired(mockExtensionContext);

            // Then - should only process once
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
        }

        @Test
        @DisplayName("Should successfully process extension context")
        void shouldSuccessfullyProcessExtensionContext() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            List<ExporterTestCase> testCases = createTestCases(2);

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            doReturn(TestClassA.class).when(mockExtensionContext).getRequiredTestClass();
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(testCases);

            // When
            methodExportManager.loadTestBodyIfRequired(mockExtensionContext);

            // Then
            verify(mockExtensionContext, times(2)).getRequiredTestClass(); // Called twice in the method
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileParser, times(1)).parseFile(filepath);
            verify(mockMethodCaseExtractor, times(1)).extractTestCases(mockCompilationUnit, filepath);
            verify(mockExportSender, times(1)).sendTestCases(testCases);
        }

        @Test
        @DisplayName("Should return early when FileFinder returns null")
        void shouldReturnEarlyWhenFileFinderReturnsNull() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            doReturn(TestClassA.class).when(mockExtensionContext).getRequiredTestClass();
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(null);

            // When
            methodExportManager.loadTestBodyIfRequired(mockExtensionContext);

            // Then
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileParser, never()).parseFile(anyString());
        }
    }

    @Nested
    @DisplayName("Property Checking Tests")
    class PropertyCheckingTests {

        @Test
        @DisplayName("Should consider export required when property exists with any value")
        void shouldConsiderExportRequiredWhenPropertyExistsWithAnyValue() throws Exception {
            // Given
            String[] propertyValues = {"true"};

            for (String value : propertyValues) {
                // Setup
                when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                        .thenReturn(value);
                when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(null); // Early return

                // When
                methodExportManager.loadTestBodyForClass(TestClassA.class);

                // Then
                verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);

                // Reset for next iteration
                clearProcessedClasses();
            }
        }

        @Test
        @DisplayName("Should consider export not required when property throws exception")
        void shouldConsiderExportNotRequiredWhenPropertyThrowsException() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenThrow(new PropertyNotFoundException("Property not found"));

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockFileFinder, never()).getTestClassFilePath(any());
        }

        @Test
        @DisplayName("Should handle property provider runtime exception")
        void shouldHandlePropertyProviderRuntimeException() {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockFileFinder, never()).getTestClassFilePath(any());
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Should handle concurrent processing of same class")
        void shouldHandleConcurrentProcessingOfSameClass() throws Exception {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class))
                    .thenReturn("src/test/java/TestClassA.java");

            // When - simulate concurrent calls
            Thread thread1 = new Thread(() -> methodExportManager.loadTestBodyForClass(TestClassA.class));
            Thread thread2 = new Thread(() -> methodExportManager.loadTestBodyForClass(TestClassA.class));
            Thread thread3 = new Thread(() -> methodExportManager.loadTestBodyForClass(TestClassA.class));

            thread1.start();
            thread2.start();
            thread3.start();

            thread1.join();
            thread2.join();
            thread3.join();

            // Then - should only process once due to ConcurrentHashMap
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
        }

        @Test
        @DisplayName("Should handle concurrent processing of different classes")
        void shouldHandleConcurrentProcessingOfDifferentClasses() throws Exception {
            // Given
            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("true");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class))
                    .thenReturn("src/test/java/TestClassA.java");
            when(mockFileFinder.getTestClassFilePath(TestClassB.class))
                    .thenReturn("src/test/java/TestClassB.java");

            // When - simulate concurrent calls for different classes
            Thread thread1 = new Thread(() -> methodExportManager.loadTestBodyForClass(TestClassA.class));
            Thread thread2 = new Thread(() -> methodExportManager.loadTestBodyForClass(TestClassB.class));

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            // Then - should process both classes
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassB.class);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete successful flow")
        void shouldHandleCompleteSuccessfulFlow() {
            // Given
            String filepath = "src/test/java/TestClassA.java";
            List<ExporterTestCase> testCases = createTestCases(5);

            when(mockPropertyProvider.getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME))
                    .thenReturn("enabled");
            when(mockFileFinder.getTestClassFilePath(TestClassA.class)).thenReturn(filepath);
            when(mockFileParser.parseFile(filepath)).thenReturn(mockCompilationUnit);
            when(mockMethodCaseExtractor.extractTestCases(mockCompilationUnit, filepath))
                    .thenReturn(testCases);

            // When
            methodExportManager.loadTestBodyForClass(TestClassA.class);

            // Then - verify complete flow
            verify(mockPropertyProvider, times(1))
                    .getProperty(MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
            verify(mockFileFinder, times(1)).getTestClassFilePath(TestClassA.class);
            verify(mockFileParser, times(1)).parseFile(filepath);
            verify(mockMethodCaseExtractor, times(1)).extractTestCases(mockCompilationUnit, filepath);
            verify(mockExportSender, times(1)).sendTestCases(testCases);
        }

        @Test
        @DisplayName("Should verify constant value")
        void shouldVerifyConstantValue() {
            // Then
            assertEquals("testomatio.export.required", MethodExportManager.EXPORT_REQUIRED_PROPERTY_NAME);
        }
    }

    // Helper methods
    private List<ExporterTestCase> createTestCases(int count) {
        List<ExporterTestCase> testCases = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ExporterTestCase testCase = new ExporterTestCase();
            testCase.setName("testMethod" + i);
            testCase.setCode("public void testMethod" + i + "() {}");
            testCase.setFile("TestClass.java");
            testCase.setSkipped(false);
            testCase.setSuites(Arrays.asList("Suite" + i));
            testCase.setLabels(Arrays.asList("Label" + i));
            testCases.add(testCase);
        }
        return testCases;
    }
}