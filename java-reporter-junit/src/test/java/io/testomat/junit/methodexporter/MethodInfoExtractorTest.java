package io.testomat.junit.methodexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ReferenceType;
import io.testomat.junit.methodexporter.extractors.LabelExtractor;
import io.testomat.junit.methodexporter.extractors.MethodInfoExtractor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("MethodInfoExtractor Tests")
class MethodInfoExtractorTest {

    @Mock
    private LabelExtractor mockLabelExtractor;

    @Mock
    private MethodDeclaration mockMethodDeclaration;

    @Mock
    private ClassOrInterfaceDeclaration mockClassDeclaration;

    private MethodInfoExtractor methodInfoExtractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        methodInfoExtractor = new MethodInfoExtractor(mockLabelExtractor);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void shouldCreateInstanceWithDefaultConstructor() {
            // When
            MethodInfoExtractor extractor = new MethodInfoExtractor();

            // Then
            assertNotNull(extractor);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void shouldCreateInstanceWithParameterizedConstructor() {
            // When
            MethodInfoExtractor extractor = new MethodInfoExtractor(mockLabelExtractor);

            // Then
            assertNotNull(extractor);
        }
    }

    @Nested
    @DisplayName("Get Test Name Tests")
    class GetTestNameTests {

        @Test
        @DisplayName("Should return DisplayName annotation value when present")
        void shouldReturnDisplayNameAnnotationValueWhenPresent() {
            // Given
            AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(displayNameAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation))
                    .thenReturn("Custom Test Name");

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("Custom Test Name", result);
        }

        @Test
        @DisplayName("Should return method name when DisplayName annotation not present")
        void shouldReturnMethodNameWhenDisplayNameAnnotationNotPresent() {
            // Given
            when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("testMethod", result);
        }

        @Test
        @DisplayName("Should return method name when DisplayName annotation value is null")
        void shouldReturnMethodNameWhenDisplayNameAnnotationValueIsNull() {
            // Given
            AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(displayNameAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation)).thenReturn(null);

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("testMethod", result);
        }

        @Test
        @DisplayName("Should return method name when exception occurs")
        void shouldReturnMethodNameWhenExceptionOccurs() {
            // Given
            when(mockMethodDeclaration.getAnnotations())
                    .thenThrow(new RuntimeException("Annotation processing error"));
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("testMethod", result);
        }

        @Test
        @DisplayName("Should handle multiple annotations and find DisplayName")
        void shouldHandleMultipleAnnotationsAndFindDisplayName() {
            // Given
            AnnotationExpr testAnnotation = createMockAnnotation("Test");
            AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");
            AnnotationExpr timeoutAnnotation = createMockAnnotation("Timeout");

            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testAnnotation);
            annotations.add(displayNameAnnotation);
            annotations.add(timeoutAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation))
                    .thenReturn("Test with Custom Name");

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("Test with Custom Name", result);
        }
    }

    @Nested
    @DisplayName("Get Method Code Tests")
    class GetMethodCodeTests {

        @Test
        @DisplayName("Should build complete method code with annotations and signature")
        void shouldBuildCompleteMethodCodeWithAnnotationsAndSignature() {
            // Given
            setupBasicMethodMocks();

            AnnotationExpr testAnnotation = mock(AnnotationExpr.class);
            when(testAnnotation.toString()).thenReturn("@Test");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testAnnotation);

            Modifier publicModifier = mock(Modifier.class);
            Modifier.Keyword publicKeyword = mock(Modifier.Keyword.class);
            when(publicKeyword.asString()).thenReturn("public");
            when(publicModifier.getKeyword()).thenReturn(publicKeyword);
            NodeList<Modifier> modifiers = new NodeList<>();
            modifiers.add(publicModifier);

            BlockStmt methodBody = mock(BlockStmt.class);
            when(methodBody.toString()).thenReturn("{ /* test code */ }");

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getModifiers()).thenReturn(modifiers);
            when(mockMethodDeclaration.getTypeAsString()).thenReturn("void");
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            when(mockMethodDeclaration.getParameters()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getThrownExceptions()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getBody()).thenReturn(Optional.of(methodBody));

            // When
            String result = methodInfoExtractor.getMethodCode(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("@Test"));
            assertTrue(result.contains("public"));
            assertTrue(result.contains("void"));
            assertTrue(result.contains("testMethod"));
            assertTrue(result.contains("{ /* test code */ }"));
        }

        @Test
        @DisplayName("Should build method code with parameters")
        void shouldBuildMethodCodeWithParameters() {
            // Given
            setupBasicMethodMocks();

            Parameter param1 = mock(Parameter.class);
            Parameter param2 = mock(Parameter.class);
            when(param1.toString()).thenReturn("String arg1");
            when(param2.toString()).thenReturn("int arg2");

            NodeList<Parameter> parameters = new NodeList<>();
            parameters.add(param1);
            parameters.add(param2);

            when(mockMethodDeclaration.getParameters()).thenReturn(parameters);
            when(mockMethodDeclaration.getParameter(0)).thenReturn(param1);
            when(mockMethodDeclaration.getParameter(1)).thenReturn(param2);

            // When
            String result = methodInfoExtractor.getMethodCode(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("String arg1, int arg2"));
        }

        @Test
        @DisplayName("Should build method code with thrown exceptions")
        void shouldBuildMethodCodeWithThrownExceptions() {
            // Given
            setupBasicMethodMocks();

            ReferenceType exception1 = mock(ReferenceType.class);
            ReferenceType exception2 = mock(ReferenceType.class);
            when(exception1.toString()).thenReturn("IOException");
            when(exception2.toString()).thenReturn("SQLException");

            NodeList<ReferenceType> exceptions = new NodeList<>();
            exceptions.add(exception1);
            exceptions.add(exception2);

            when(mockMethodDeclaration.getThrownExceptions()).thenReturn(exceptions);
            when(mockMethodDeclaration.getThrownException(0)).thenReturn(exception1);
            when(mockMethodDeclaration.getThrownException(1)).thenReturn(exception2);

            // When
            String result = methodInfoExtractor.getMethodCode(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertTrue(result.contains("throws IOException, SQLException"));
        }

        @Test
        @DisplayName("Should build method code without body")
        void shouldBuildMethodCodeWithoutBody() {
            // Given
            setupBasicMethodMocks();
            when(mockMethodDeclaration.getBody()).thenReturn(Optional.empty());

            // When
            String result = methodInfoExtractor.getMethodCode(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertFalse(result.contains("{"));
        }

        @Test
        @DisplayName("Should return toString when exception occurs")
        void shouldReturnToStringWhenExceptionOccurs() {
            // Given
            when(mockMethodDeclaration.getAnnotations())
                    .thenThrow(new RuntimeException("Code building error"));
            when(mockMethodDeclaration.toString()).thenReturn("fallback method code");

            // When
            String result = methodInfoExtractor.getMethodCode(mockMethodDeclaration);

            // Then
            assertEquals("fallback method code", result);
        }
    }

    @Nested
    @DisplayName("Is Test Skipped Tests")
    class IsTestSkippedTests {

        @Test
        @DisplayName("Should return true when method has Disabled annotation")
        void shouldReturnTrueWhenMethodHasDisabledAnnotation() {
            // Given
            AnnotationExpr disabledAnnotation = createMockAnnotation("Disabled");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(disabledAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when method has Ignore annotation")
        void shouldReturnTrueWhenMethodHasIgnoreAnnotation() {
            // Given
            AnnotationExpr ignoreAnnotation = createMockAnnotation("Ignore");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(ignoreAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when method name starts with ignore")
        void shouldReturnTrueWhenMethodNameStartsWithIgnore() {
            // Given
            when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getNameAsString()).thenReturn("ignoreThisTest");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when method name starts with skip")
        void shouldReturnTrueWhenMethodNameStartsWithSkip() {
            // Given
            when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
            when(mockMethodDeclaration.getNameAsString()).thenReturn("skipThisTest");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when both annotation and name indicate skip")
        void shouldReturnTrueWhenBothAnnotationAndNameIndicateSkip() {
            // Given
            AnnotationExpr disabledAnnotation = createMockAnnotation("Disabled");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(disabledAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("ignoreThisTest");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when method is not skipped")
        void shouldReturnFalseWhenMethodIsNotSkipped() {
            // Given
            AnnotationExpr testAnnotation = createMockAnnotation("Test");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(testAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when exception occurs")
        void shouldReturnFalseWhenExceptionOccurs() {
            // Given
            when(mockMethodDeclaration.getAnnotations())
                    .thenThrow(new RuntimeException("Skip check error"));

            // When
            boolean result = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Extract Suites Tests")
    class ExtractSuitesTests {

        @Test
        @DisplayName("Should extract single suite from single class")
        void shouldExtractSingleSuiteFromSingleClass() {
            // Given
            when(mockMethodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(mockClassDeclaration));
            when(mockClassDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.empty());
            when(mockClassDeclaration.getAnnotationByName("DisplayName"))
                    .thenReturn(Optional.empty());
            when(mockClassDeclaration.getNameAsString()).thenReturn("TestClass");

            // When
            List<String> result = methodInfoExtractor.extractSuites(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TestClass", result.get(0));
        }

        @Test
        @DisplayName("Should extract suite with DisplayName annotation")
        void shouldExtractSuiteWithDisplayNameAnnotation() {
            // Given
            AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");

            when(mockMethodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(mockClassDeclaration));
            when(mockClassDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.empty());
            when(mockClassDeclaration.getAnnotationByName("DisplayName"))
                    .thenReturn(Optional.of(displayNameAnnotation));
            when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation))
                    .thenReturn("Custom Suite Name");
            when(mockClassDeclaration.getNameAsString()).thenReturn("TestClass");

            // When
            List<String> result = methodInfoExtractor.extractSuites(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Custom Suite Name", result.get(0));
        }

        @Test
        @DisplayName("Should extract nested suites in hierarchical order")
        void shouldExtractNestedSuitesInHierarchicalOrder() {
            // Given
            ClassOrInterfaceDeclaration outerClass = mock(ClassOrInterfaceDeclaration.class);
            ClassOrInterfaceDeclaration innerClass = mock(ClassOrInterfaceDeclaration.class);

            // Setup hierarchy: method -> innerClass -> outerClass -> null
            when(mockMethodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(innerClass));
            when(innerClass.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(outerClass));
            when(outerClass.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.empty());

            // Setup class names
            when(outerClass.getAnnotationByName("DisplayName")).thenReturn(Optional.empty());
            when(outerClass.getNameAsString()).thenReturn("OuterTestClass");
            when(innerClass.getAnnotationByName("DisplayName")).thenReturn(Optional.empty());
            when(innerClass.getNameAsString()).thenReturn("InnerTestClass");

            // When
            List<String> result = methodInfoExtractor.extractSuites(mockMethodDeclaration);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("OuterTestClass", result.get(0)); // Outermost first
            assertEquals("InnerTestClass", result.get(1)); // Innermost last
        }

        @Test
        @DisplayName("Should handle normal suite extraction without errors")
        void shouldHandleNormalSuiteExtractionWithoutErrors() {
            // Given - simple normal case
            MethodDeclaration normalMethod = mock(MethodDeclaration.class);
            ClassOrInterfaceDeclaration normalClass = mock(ClassOrInterfaceDeclaration.class);

            when(normalMethod.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(normalClass));
            when(normalClass.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.empty());
            when(normalClass.getAnnotationByName("DisplayName"))
                    .thenReturn(Optional.empty());
            when(normalClass.getNameAsString()).thenReturn("NormalTestClass");

            // When
            List<String> result = methodInfoExtractor.extractSuites(normalMethod);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("NormalTestClass", result.get(0));
        }

        @Test
        @DisplayName("Should return fallback suites when hierarchy building fails")
        void shouldReturnFallbackSuitesWhenHierarchyBuildingFails() {
            // Given - simulate exception during hierarchy collection, not findAncestor
            MethodDeclaration testMethod = mock(MethodDeclaration.class);
            ClassOrInterfaceDeclaration testClass = mock(ClassOrInterfaceDeclaration.class);

            // Setup successful findAncestor for fallback
            when(testMethod.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.of(testClass));
            when(testClass.getNameAsString()).thenReturn("FallbackClass");

            // But make hierarchy building fail by having the class throw exception
            // when trying to traverse up the hierarchy
            when(testClass.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenThrow(new RuntimeException("Hierarchy traversal error"));

            // When
            List<String> result = methodInfoExtractor.extractSuites(testMethod);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("FallbackClass", result.get(0));
        }

        @Test
        @DisplayName("Should return empty fallback suites when no class found")
        void shouldReturnEmptyFallbackSuitesWhenNoClassFound() {
            // Given - simulate successful main flow that returns empty, no exception needed
            MethodDeclaration testMethod = mock(MethodDeclaration.class);

            // No ancestor class found
            when(testMethod.findAncestor(ClassOrInterfaceDeclaration.class))
                    .thenReturn(Optional.empty());

            // When
            List<String> result = methodInfoExtractor.extractSuites(testMethod);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should extract DisplayName correctly in isolation")
        void shouldExtractDisplayNameCorrectlyInIsolation() {
            // Given - simplified test for DisplayName extraction
            AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");
            NodeList<AnnotationExpr> annotations = new NodeList<>();
            annotations.add(displayNameAnnotation);

            when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
            when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
            when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation))
                    .thenReturn("Custom Test Name");

            // When
            String result = methodInfoExtractor.getTestName(mockMethodDeclaration);

            // Then
            assertEquals("Custom Test Name", result);
        }

        @Test
        @DisplayName("Should handle complete method with all features")
        void shouldHandleCompleteMethodWithAllFeatures() {
            // Given
            setupCompleteMethodMocks();

            // When - test all methods together
            String testName = methodInfoExtractor.getTestName(mockMethodDeclaration);
            String methodCode = methodInfoExtractor.getMethodCode(mockMethodDeclaration);
            boolean isSkipped = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);
            List<String> suites = methodInfoExtractor.extractSuites(mockMethodDeclaration);

            // Then
            assertEquals("Complete Integration Test", testName);
            assertNotNull(methodCode);
            assertTrue(methodCode.contains("@Test"));
            assertTrue(methodCode.contains("@DisplayName"));
            assertFalse(isSkipped);
            assertEquals(1, suites.size());
            assertEquals("Integration Suite", suites.get(0));
        }

        @Test
        @DisplayName("Should handle method with skip conditions")
        void shouldHandleMethodWithSkipConditions() {
            // Given
            setupSkippedMethodMocks();

            // When
            String testName = methodInfoExtractor.getTestName(mockMethodDeclaration);
            boolean isSkipped = methodInfoExtractor.isTestSkipped(mockMethodDeclaration);

            // Then
            assertEquals("ignoreThisTest", testName); // No DisplayName, uses method name
            assertTrue(isSkipped); // Has both @Disabled and ignore prefix
        }
    }

    // Helper methods
    private AnnotationExpr createMockAnnotation(String name) {
        AnnotationExpr annotation = mock(AnnotationExpr.class);
        when(annotation.getNameAsString()).thenReturn(name);
        return annotation;
    }

    private void setupBasicMethodMocks() {
        when(mockMethodDeclaration.getAnnotations()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getModifiers()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getTypeAsString()).thenReturn("void");
        when(mockMethodDeclaration.getNameAsString()).thenReturn("testMethod");
        when(mockMethodDeclaration.getParameters()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getThrownExceptions()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getBody()).thenReturn(Optional.empty());
    }

    private void setupCompleteMethodMocks() {
        // Setup annotations
        AnnotationExpr testAnnotation = mock(AnnotationExpr.class);
        AnnotationExpr displayNameAnnotation = createMockAnnotation("DisplayName");
        when(testAnnotation.toString()).thenReturn("@Test");
        when(testAnnotation.getNameAsString()).thenReturn("Test");
        when(displayNameAnnotation.toString()).thenReturn("@DisplayName(\"Complete Integration Test\")");

        NodeList<AnnotationExpr> annotations = new NodeList<>();
        annotations.add(testAnnotation);
        annotations.add(displayNameAnnotation);

        when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
        when(mockMethodDeclaration.getNameAsString()).thenReturn("testCompleteFeature");
        when(mockLabelExtractor.getAnnotationValue(displayNameAnnotation))
                .thenReturn("Complete Integration Test");

        // Setup method signature (without overriding annotations)
        when(mockMethodDeclaration.getModifiers()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getTypeAsString()).thenReturn("void");
        when(mockMethodDeclaration.getParameters()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getThrownExceptions()).thenReturn(new NodeList<>());
        when(mockMethodDeclaration.getBody()).thenReturn(Optional.empty());

        // Setup class hierarchy
        AnnotationExpr classDisplayNameAnnotation = createMockAnnotation("DisplayName");
        when(mockMethodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                .thenReturn(Optional.of(mockClassDeclaration));
        when(mockClassDeclaration.findAncestor(ClassOrInterfaceDeclaration.class))
                .thenReturn(Optional.empty());
        when(mockClassDeclaration.getAnnotationByName("DisplayName"))
                .thenReturn(Optional.of(classDisplayNameAnnotation));
        when(mockLabelExtractor.getAnnotationValue(classDisplayNameAnnotation))
                .thenReturn("Integration Suite");
    }

    private void setupSkippedMethodMocks() {
        // Setup skip annotations
        AnnotationExpr disabledAnnotation = createMockAnnotation("Disabled");
        NodeList<AnnotationExpr> annotations = new NodeList<>();
        annotations.add(disabledAnnotation);

        when(mockMethodDeclaration.getAnnotations()).thenReturn(annotations);
        when(mockMethodDeclaration.getNameAsString()).thenReturn("ignoreThisTest");

        // No DisplayName annotation
        // Method name starts with "ignore" prefix
        // Has @Disabled annotation
    }
}