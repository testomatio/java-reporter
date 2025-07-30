package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.testomat.junit.exception.ExtractionException;
import io.testomat.junit.exception.MethodExporterException;
import io.testomat.junit.methodexporter.extractors.LabelExtractor;
import io.testomat.junit.methodexporter.extractors.MethodCaseExtractor;
import io.testomat.junit.methodexporter.extractors.MethodInfoExtractor;
import io.testomat.junit.methodexporter.filefinder.FileFinder;
import io.testomat.junit.model.ExporterTestCase;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("MethodCaseExtractor Tests")
class MethodCaseExtractorTest {

    @Mock
    private MethodInfoExtractor mockMethodInfoExtractor;
    
    @Mock
    private LabelExtractor mockLabelExtractor;
    
    @Mock
    private FileFinder mockFileFinder;
    
    @Mock
    private CompilationUnit mockCompilationUnit;
    
    private MethodCaseExtractor methodCaseExtractor;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        methodCaseExtractor = new MethodCaseExtractor(
            mockMethodInfoExtractor,
            mockLabelExtractor,
            mockFileFinder
        );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create instance with default constructor")
        void shouldCreateInstanceWithDefaultConstructor() {
            // When
            MethodCaseExtractor extractor = new MethodCaseExtractor();
            
            // Then
            assertNotNull(extractor);
        }
        
        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void shouldCreateInstanceWithParameterizedConstructor() {
            // When
            MethodCaseExtractor extractor = new MethodCaseExtractor(
                mockMethodInfoExtractor,
                mockLabelExtractor,
                mockFileFinder
            );
            
            // Then
            assertNotNull(extractor);
        }
    }

    @Nested
    @DisplayName("Extract Test Cases Tests")
    class ExtractTestCasesTests {
        
        @Test
        @DisplayName("Should extract test cases from compilation unit with test methods")
        void shouldExtractTestCasesFromCompilationUnitWithTestMethods() {
            // Given
            String filepath = "src/test/java/TestClass.java";
            List<MethodDeclaration> allMethods = createMethods(
                createTestMethod("testMethod1", "Test"),
                createTestMethod("testMethod2", "ParameterizedTest"),
                createNonTestMethod("helperMethod")
            );
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(allMethods);
            setupMethodInfoExtractorMocks();
            when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
                .thenReturn(Arrays.asList("unit"));
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("TestClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(2, result.size()); // Only test methods should be extracted
            
            verify(mockMethodInfoExtractor, times(2)).getTestName(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).getMethodCode(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).isTestSkipped(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).extractSuites(any(MethodDeclaration.class));
            verify(mockLabelExtractor, times(2)).extractLabels(any(MethodDeclaration.class));
            verify(mockFileFinder, times(2)).extractRelativeFilePath(filepath);
        }
        
        @Test
        @DisplayName("Should return empty list when compilation unit has no methods")
        void shouldReturnEmptyListWhenCompilationUnitHasNoMethods() {
            // Given
            String filepath = "src/test/java/EmptyClass.java";
            when(mockCompilationUnit.findAll(MethodDeclaration.class))
                .thenReturn(Collections.emptyList());
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            
            verify(mockMethodInfoExtractor, never()).getTestName(any());
            verify(mockLabelExtractor, never()).extractLabels(any());
            verify(mockFileFinder, never()).extractRelativeFilePath(anyString());
        }
        
        @Test
        @DisplayName("Should return empty list when compilation unit has no test methods")
        void shouldReturnEmptyListWhenCompilationUnitHasNoTestMethods() {
            // Given
            String filepath = "src/test/java/UtilClass.java";
            List<MethodDeclaration> allMethods = createMethods(
                createNonTestMethod("helperMethod1"),
                createNonTestMethod("helperMethod2"),
                createNonTestMethod("utilityMethod")
            );
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(allMethods);
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            
            verify(mockMethodInfoExtractor, never()).getTestName(any());
            verify(mockLabelExtractor, never()).extractLabels(any());
            verify(mockFileFinder, never()).extractRelativeFilePath(anyString());
        }
        
        @Test
        @DisplayName("Should extract only test methods from mixed method compilation unit")
        void shouldExtractOnlyTestMethodsFromMixedMethodCompilationUnit() {
            // Given
            String filepath = "src/test/java/MixedClass.java";
            List<MethodDeclaration> allMethods = createMethods(
                createNonTestMethod("setUp"),
                createTestMethod("testFeatureA", "Test"),
                createNonTestMethod("helperMethod"),
                createTestMethod("testFeatureB", "RepeatedTest"),
                createNonTestMethod("tearDown"),
                createTestMethod("testFeatureC", "TestFactory")
            );
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(allMethods);
            setupMethodInfoExtractorMocks();
            when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
                .thenReturn(Arrays.asList("unit"));
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("MixedClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // Only test methods
            
            verify(mockMethodInfoExtractor, times(3)).getTestName(any(MethodDeclaration.class));
        }
        
        @Test
        @DisplayName("Should handle all supported test annotations")
        void shouldHandleAllSupportedTestAnnotations() {
            // Given
            String filepath = "src/test/java/AnnotationTest.java";
            List<MethodDeclaration> allMethods = createMethods(
                createTestMethod("testSimple", "Test"),
                createTestMethod("testParameterized", "ParameterizedTest"),
                createTestMethod("testRepeated", "RepeatedTest"),
                createTestMethod("testDynamic", "TestFactory")
            );
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(allMethods);
            setupMethodInfoExtractorMocks();
            when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
                .thenReturn(Arrays.asList("unit"));
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("AnnotationTest.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(4, result.size()); // All test methods should be extracted
        }
    }

    @Nested
    @DisplayName("Test Method Detection Tests")
    class TestMethodDetectionTests {
        
        @Test
        @DisplayName("Should identify method with Test annotation as test method")
        void shouldIdentifyMethodWithTestAnnotationAsTestMethod() {
            // Given
            String filepath = "src/test/java/TestClass.java";
            MethodDeclaration testMethod = createTestMethod("testMethod", "Test");
            List<MethodDeclaration> methods = Arrays.asList(testMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            setupMethodInfoExtractorMocks();
            when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
                .thenReturn(Arrays.asList("unit"));
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("TestClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
        
        @Test
        @DisplayName("Should not identify method with non-test annotation as test method")
        void shouldNotIdentifyMethodWithNonTestAnnotationAsTestMethod() {
            // Given
            String filepath = "src/test/java/TestClass.java";
            MethodDeclaration nonTestMethod = createMethodWithAnnotation("setupMethod", "BeforeEach");
            List<MethodDeclaration> methods = Arrays.asList(nonTestMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle method annotation exception gracefully")
        void shouldHandleMethodAnnotationExceptionGracefully() {
            // Given
            String filepath = "src/test/java/TestClass.java";
            MethodDeclaration problematicMethod = mock(MethodDeclaration.class);
            when(problematicMethod.getAnnotations())
                .thenThrow(new RuntimeException("Annotation processing error"));
            
            List<MethodDeclaration> methods = Arrays.asList(problematicMethod);
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty()); // Method should be filtered out due to exception
        }
    }

    @Nested
    @DisplayName("Test Case Creation Tests")
    class TestCaseCreationTests {
        
        @Test
        @DisplayName("Should create complete test case with all fields populated")
        void shouldCreateCompleteTestCaseWithAllFieldsPopulated() {
            // Given
            String filepath = "src/test/java/CompleteTestClass.java";
            MethodDeclaration testMethod = createTestMethod("testCompleteFeature", "Test");
            List<MethodDeclaration> methods = Arrays.asList(testMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            
            // Setup detailed mock responses
            when(mockMethodInfoExtractor.getTestName(testMethod)).thenReturn("Complete Feature Test");
            when(mockMethodInfoExtractor.getMethodCode(testMethod)).thenReturn("public void testCompleteFeature() { /* test code */ }");
            when(mockMethodInfoExtractor.isTestSkipped(testMethod)).thenReturn(false);
            when(mockMethodInfoExtractor.extractSuites(testMethod)).thenReturn(Arrays.asList("TestSuite", "FeatureSuite"));
            when(mockLabelExtractor.extractLabels(testMethod)).thenReturn(Arrays.asList("unit", "feature"));
            when(mockFileFinder.extractRelativeFilePath(filepath)).thenReturn("CompleteTestClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            
            ExporterTestCase testCase = result.get(0);
            assertEquals("Complete Feature Test", testCase.getName());
            assertEquals("public void testCompleteFeature() { /* test code */ }", testCase.getCode());
            assertFalse(testCase.isSkipped());
            assertEquals(Arrays.asList("TestSuite", "FeatureSuite"), testCase.getSuites());
            assertEquals(Arrays.asList("unit", "feature"), testCase.getLabels());
            assertEquals("CompleteTestClass.java", testCase.getFile());
        }
        
        @Test
        @DisplayName("Should create test case with skipped flag set to true")
        void shouldCreateTestCaseWithSkippedFlagSetToTrue() {
            // Given
            String filepath = "src/test/java/SkippedTestClass.java";
            MethodDeclaration skippedTestMethod = createTestMethod("testSkippedFeature", "Test");
            List<MethodDeclaration> methods = Arrays.asList(skippedTestMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            
            when(mockMethodInfoExtractor.getTestName(skippedTestMethod)).thenReturn("testSkippedFeature");
            when(mockMethodInfoExtractor.getMethodCode(skippedTestMethod)).thenReturn("public void testSkippedFeature() {}");
            when(mockMethodInfoExtractor.isTestSkipped(skippedTestMethod)).thenReturn(true); // Skipped
            when(mockMethodInfoExtractor.extractSuites(skippedTestMethod)).thenReturn(Arrays.asList("TestSuite"));
            when(mockLabelExtractor.extractLabels(skippedTestMethod)).thenReturn(Arrays.asList("unit", "disabled"));
            when(mockFileFinder.extractRelativeFilePath(filepath)).thenReturn("SkippedTestClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            
            ExporterTestCase testCase = result.get(0);
            assertTrue(testCase.isSkipped());
            assertTrue(testCase.getLabels().contains("disabled"));
        }
        
        @Test
        @DisplayName("Should throw ExtractionException when createTestCase fails")
        void shouldThrowExtractionExceptionWhenCreateTestCaseFails() {
            // Given
            String filepath = "src/test/java/FailingTestClass.java";
            MethodDeclaration testMethod = createTestMethod("testMethod", "Test");
            List<MethodDeclaration> methods = Arrays.asList(testMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            when(mockMethodInfoExtractor.getTestName(testMethod))
                .thenThrow(new RuntimeException("Failed to extract test name"));
            
            // When & Then
            ExtractionException exception = assertThrows(
                ExtractionException.class,
                () -> methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath)
            );
            
            assertEquals("Failed to convert List<MethodDeclaration> to List<ExporterTestCase>", 
                exception.getMessage());
        }
        
        @Test
        @DisplayName("Should wrap createTestCase exception in MethodExporterException")
        void shouldWrapCreateTestCaseExceptionInMethodExporterException() {
            // Given
            String filepath = "src/test/java/ExceptionTestClass.java";
            MethodDeclaration testMethod = createTestMethod("testMethod", "Test");
            List<MethodDeclaration> methods = Arrays.asList(testMethod);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            
            // Make MethodInfoExtractor throw exception
            when(mockMethodInfoExtractor.getTestName(testMethod))
                .thenReturn("testMethod");
            when(mockMethodInfoExtractor.getMethodCode(testMethod))
                .thenReturn("public void testMethod() {}");
            when(mockMethodInfoExtractor.isTestSkipped(testMethod))
                .thenReturn(false);
            when(mockMethodInfoExtractor.extractSuites(testMethod))
                .thenThrow(new RuntimeException("Failed to extract suites"));
            
            // When & Then
            ExtractionException exception = assertThrows(
                ExtractionException.class,
                () -> methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath)
            );
            
            assertNotNull(exception.getCause());
            assertTrue(exception.getCause() instanceof MethodExporterException);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should handle complex compilation unit with multiple test methods")
        void shouldHandleComplexCompilationUnitWithMultipleTestMethods() {
            // Given
            String filepath = "src/test/java/ComplexTestClass.java";
            List<MethodDeclaration> allMethods = createMethods(
                createNonTestMethod("setUp"),
                createTestMethod("testFeatureA", "Test"),
                createTestMethod("testFeatureB", "ParameterizedTest"),
                createNonTestMethod("helperMethod"),
                createTestMethod("testFeatureC", "RepeatedTest"),
                createNonTestMethod("tearDown")
            );
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(allMethods);
            setupVariedMethodInfoExtractorMocks();
            setupVariedLabelExtractorMocks();
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("ComplexTestClass.java");
            
            // When
            List<ExporterTestCase> result = methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // Only test methods
            
            // Verify all test cases have required fields
            for (ExporterTestCase testCase : result) {
                assertNotNull(testCase.getName());
                assertNotNull(testCase.getCode());
                assertNotNull(testCase.getSuites());
                assertNotNull(testCase.getLabels());
                assertNotNull(testCase.getFile());
            }
        }
        
        @Test
        @DisplayName("Should verify all dependencies are called correctly")
        void shouldVerifyAllDependenciesAreCalledCorrectly() {
            // Given
            String filepath = "src/test/java/DependencyTestClass.java";
            MethodDeclaration testMethod1 = createTestMethod("testMethod1", "Test");
            MethodDeclaration testMethod2 = createTestMethod("testMethod2", "ParameterizedTest");
            List<MethodDeclaration> methods = Arrays.asList(testMethod1, testMethod2);
            
            when(mockCompilationUnit.findAll(MethodDeclaration.class)).thenReturn(methods);
            setupMethodInfoExtractorMocks();
            when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
                .thenReturn(Arrays.asList("unit"));
            when(mockFileFinder.extractRelativeFilePath(filepath))
                .thenReturn("DependencyTestClass.java");
            
            // When
            methodCaseExtractor.extractTestCases(mockCompilationUnit, filepath);
            
            // Then
            verify(mockCompilationUnit, times(1)).findAll(MethodDeclaration.class);
            verify(mockMethodInfoExtractor, times(2)).getTestName(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).getMethodCode(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).isTestSkipped(any(MethodDeclaration.class));
            verify(mockMethodInfoExtractor, times(2)).extractSuites(any(MethodDeclaration.class));
            verify(mockLabelExtractor, times(2)).extractLabels(any(MethodDeclaration.class));
            verify(mockFileFinder, times(2)).extractRelativeFilePath(filepath);
        }
    }

    // Helper methods
    private List<MethodDeclaration> createMethods(MethodDeclaration... methods) {
        return Arrays.asList(methods);
    }
    
    private MethodDeclaration createTestMethod(String methodName, String annotationName) {
        MethodDeclaration method = mock(MethodDeclaration.class);
        AnnotationExpr annotation = mock(AnnotationExpr.class);
        NodeList<AnnotationExpr> annotations = new NodeList<>();
        annotations.add(annotation);
        
        when(method.getAnnotations()).thenReturn(annotations);
        when(annotation.getNameAsString()).thenReturn(annotationName);
        when(method.getNameAsString()).thenReturn(methodName);
        
        return method;
    }
    
    private MethodDeclaration createNonTestMethod(String methodName) {
        MethodDeclaration method = mock(MethodDeclaration.class);
        when(method.getAnnotations()).thenReturn(new NodeList<>());
        when(method.getNameAsString()).thenReturn(methodName);
        return method;
    }
    
    private MethodDeclaration createMethodWithAnnotation(String methodName, String annotationName) {
        MethodDeclaration method = mock(MethodDeclaration.class);
        AnnotationExpr annotation = mock(AnnotationExpr.class);
        NodeList<AnnotationExpr> annotations = new NodeList<>();
        annotations.add(annotation);
        
        when(method.getAnnotations()).thenReturn(annotations);
        when(annotation.getNameAsString()).thenReturn(annotationName);
        when(method.getNameAsString()).thenReturn(methodName);
        
        return method;
    }
    
    private void setupMethodInfoExtractorMocks() {
        when(mockMethodInfoExtractor.getTestName(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                return method.getNameAsString();
            });
        when(mockMethodInfoExtractor.getMethodCode(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                return "public void " + method.getNameAsString() + "() {}";
            });
        when(mockMethodInfoExtractor.isTestSkipped(any(MethodDeclaration.class)))
            .thenReturn(false);
        when(mockMethodInfoExtractor.extractSuites(any(MethodDeclaration.class)))
            .thenReturn(Arrays.asList("TestSuite"));
    }
    
    private void setupVariedMethodInfoExtractorMocks() {
        when(mockMethodInfoExtractor.getTestName(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                return "Test: " + method.getNameAsString();
            });
        when(mockMethodInfoExtractor.getMethodCode(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                return "public void " + method.getNameAsString() + "() { /* implementation */ }";
            });
        when(mockMethodInfoExtractor.isTestSkipped(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                return method.getNameAsString().contains("Skip");
            });
        when(mockMethodInfoExtractor.extractSuites(any(MethodDeclaration.class)))
            .thenReturn(Arrays.asList("MainSuite", "SubSuite"));
    }
    
    private void setupVariedLabelExtractorMocks() {
        when(mockLabelExtractor.extractLabels(any(MethodDeclaration.class)))
            .thenAnswer(invocation -> {
                MethodDeclaration method = invocation.getArgument(0);
                String methodName = method.getNameAsString();
                if (methodName.contains("Integration")) {
                    return Arrays.asList("unit", "integration");
                } else if (methodName.contains("Performance")) {
                    return Arrays.asList("unit", "performance");
                } else {
                    return Arrays.asList("unit");
                }
            });
    }
}