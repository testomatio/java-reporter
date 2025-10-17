package io.testomat.testng.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;

class TestNgTestWrapperTest {

    @Test
    @DisplayName("Should create wrapper for regular test")
    void shouldCreateWrapperForRegularTest() {
        // Given
        ITestResult testResult = mock(ITestResult.class);

        // When
        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // Then
        assertNotNull(wrapper);
        assertTrue(wrapper.isRegularTest());
        assertEquals(testResult, wrapper.getTestResult());
        assertNull(wrapper.getMethod());
        assertNull(wrapper.getTestClass());
    }

    @Test
    @DisplayName("Should create wrapper for disabled test")
    void shouldCreateWrapperForDisabledTest() throws Exception {
        // Given
        Method method = TestNgTestWrapperTest.class.getDeclaredMethod("shouldCreateWrapperForDisabledTest");
        Class<?> testClass = TestNgTestWrapperTest.class;

        // When
        TestNgTestWrapper wrapper = new TestNgTestWrapper(method, testClass);

        // Then
        assertNotNull(wrapper);
        assertFalse(wrapper.isRegularTest());
        assertNull(wrapper.getTestResult());
        assertEquals(method, wrapper.getMethod());
        assertEquals(testClass, wrapper.getTestClass());
    }

    @Test
    @DisplayName("Factory method should create wrapper for regular test")
    void factoryMethodShouldCreateWrapperForRegularTest() {
        // Given
        ITestResult testResult = mock(ITestResult.class);

        // When
        TestNgTestWrapper wrapper = TestNgTestWrapper.forRegularTest(testResult);

        // Then
        assertNotNull(wrapper);
        assertTrue(wrapper.isRegularTest());
        assertEquals(testResult, wrapper.getTestResult());
    }

    @Test
    @DisplayName("Factory method should create wrapper for disabled test")
    void factoryMethodShouldCreateWrapperForDisabledTest() throws Exception {
        // Given
        Method method = TestNgTestWrapperTest.class.getDeclaredMethod("factoryMethodShouldCreateWrapperForDisabledTest");
        Class<?> testClass = TestNgTestWrapperTest.class;

        // When
        TestNgTestWrapper wrapper = TestNgTestWrapper.forDisabledTest(method, testClass);

        // Then
        assertNotNull(wrapper);
        assertFalse(wrapper.isRegularTest());
        assertEquals(method, wrapper.getMethod());
        assertEquals(testClass, wrapper.getTestClass());
    }

    @Test
    @DisplayName("toString should return correct format for regular test")
    void toStringShouldReturnCorrectFormatForRegularTest() {
        // Given
        ITestResult testResult = mock(ITestResult.class);
        ITestNGMethod testNGMethod = mock(ITestNGMethod.class);
        ITestClass testClass = mock(ITestClass.class);

        when(testResult.getTestClass()).thenReturn(testClass);
        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testNGMethod.getMethodName()).thenReturn("testMethod");

        TestNgTestWrapper wrapper = new TestNgTestWrapper(testResult);

        // When
        String result = wrapper.toString();

        // Then
        assertTrue(result.contains("REGULAR_TEST"));
        assertTrue(result.contains("com.example.TestClass"));
        assertTrue(result.contains("testMethod"));
    }

    @Test
    @DisplayName("toString should return correct format for disabled test")
    void toStringShouldReturnCorrectFormatForDisabledTest() throws Exception {
        // Given
        Method method = TestNgTestWrapperTest.class.getDeclaredMethod("toStringShouldReturnCorrectFormatForDisabledTest");
        Class<?> testClass = TestNgTestWrapperTest.class;

        TestNgTestWrapper wrapper = new TestNgTestWrapper(method, testClass);

        // When
        String result = wrapper.toString();

        // Then
        assertTrue(result.contains("DISABLED_TEST"));
        assertTrue(result.contains("TestNgTestWrapperTest"));
        assertTrue(result.contains("toStringShouldReturnCorrectFormatForDisabledTest"));
    }
}
