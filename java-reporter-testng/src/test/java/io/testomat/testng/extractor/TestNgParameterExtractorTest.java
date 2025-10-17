package io.testomat.testng.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import org.testng.annotations.Parameters;
import org.testng.internal.ConstructorOrMethod;

class TestNgParameterExtractorTest {

    private TestNgParameterExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TestNgParameterExtractor();
    }

    @Test
    @DisplayName("Should return null when no parameters")
    void shouldReturnNullWhenNoParameters() {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        when(testResult.getParameters()).thenReturn(new Object[0]);

        // When
        Object result = extractor.extractExample(testResult);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return single parameter directly")
    void shouldReturnSingleParameterDirectly() {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        String parameter = "testValue";
        when(testResult.getParameters()).thenReturn(new Object[]{parameter});

        // When
        Object result = extractor.extractExample(testResult);

        // Then
        assertEquals(parameter, result);
    }

    @Test
    @DisplayName("Should return map for multiple parameters with fallback names")
    void shouldReturnMapForMultipleParametersWithFallbackNames() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithoutAnnotation", String.class, Integer.class);

        when(testResult.getParameters()).thenReturn(new Object[]{"value1", 123});
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        Object result = extractor.extractExample(testResult);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("value1", resultMap.get("param0"));
        assertEquals(123, resultMap.get("param1"));
    }

    @Test
    @DisplayName("Should generate RID without parameters")
    void shouldGenerateRidWithoutParameters() {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        org.testng.ITestClass testClass = mock(org.testng.ITestClass.class);

        when(testResult.getParameters()).thenReturn(new Object[0]);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");

        // When
        String rid = extractor.generateRid(testResult);

        // Then
        assertEquals("com.example.TestClass.testMethod", rid);
    }

    @Test
    @DisplayName("Should generate RID with simple parameter")
    void shouldGenerateRidWithSimpleParameter() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        org.testng.ITestClass testClass = mock(org.testng.ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithoutAnnotation", String.class, Integer.class);

        when(testResult.getParameters()).thenReturn(new Object[]{"simpleValue"});
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        String rid = extractor.generateRid(testResult);

        // Then
        assertEquals("com.example.TestClass.testMethod-param0_simpleValue", rid);
    }

    @Test
    @DisplayName("Should generate RID with complex parameter using hash")
    void shouldGenerateRidWithComplexParameterUsingHash() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        org.testng.ITestClass testClass = mock(org.testng.ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithoutAnnotation", String.class, Integer.class);

        String complexValue = "This is a very long parameter value that will be hashed";
        when(testResult.getParameters()).thenReturn(new Object[]{complexValue});
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        String rid = extractor.generateRid(testResult);

        // Then
        assertTrue(rid.startsWith("com.example.TestClass.testMethod-param0_h"));
        assertTrue(rid.contains("_h"));
    }

    @Test
    @DisplayName("Should generate RID with null parameter")
    void shouldGenerateRidWithNullParameter() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        org.testng.ITestClass testClass = mock(org.testng.ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithoutAnnotation", String.class, Integer.class);

        when(testResult.getParameters()).thenReturn(new Object[]{null});
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        String rid = extractor.generateRid(testResult);

        // Then
        assertEquals("com.example.TestClass.testMethod-param0_null", rid);
    }

    @Test
    @DisplayName("Should generate RID with multiple parameters")
    void shouldGenerateRidWithMultipleParameters() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        org.testng.ITestClass testClass = mock(org.testng.ITestClass.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithoutAnnotation", String.class, Integer.class);

        when(testResult.getParameters()).thenReturn(new Object[]{"value1", 123});
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        String rid = extractor.generateRid(testResult);

        // Then
        assertEquals("com.example.TestClass.testMethod-param0_value1-param1_123", rid);
    }

    @Test
    @DisplayName("Should use @Parameters annotation names when available")
    void shouldUseParametersAnnotationNamesWhenAvailable() throws Exception {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ConstructorOrMethod constructorOrMethod = mock(ConstructorOrMethod.class);
        Method method = TestNgParameterExtractorTest.class.getDeclaredMethod("dummyMethodWithAnnotation", String.class, Integer.class);

        when(testResult.getParameters()).thenReturn(new Object[]{"testUser", 25});
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);

        // When
        Object result = extractor.extractExample(testResult);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("testUser", resultMap.get("username"));
        assertEquals(25, resultMap.get("age"));
    }

    // Dummy methods for reflection testing
    @SuppressWarnings("unused")
    private void dummyMethodWithoutAnnotation(String param0, Integer param1) {
    }

    @Parameters({"username", "age"})
    @SuppressWarnings("unused")
    private void dummyMethodWithAnnotation(String username, Integer age) {
    }
}
