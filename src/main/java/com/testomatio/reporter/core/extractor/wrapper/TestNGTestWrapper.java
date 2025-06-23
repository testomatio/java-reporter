package com.testomatio.reporter.core.extractor.wrapper;

import java.lang.reflect.Method;
import org.testng.ITestResult;

/**
 * Context wrapper for TestNG tests that unifies handling of both regular and disabled tests.
 * This class allows the TestNGMetaDataExtractor to use a single extractTestMetadata method
 * for both types of tests by encapsulating different input data types.
 */
public class TestNGTestWrapper {
    private final ITestResult testResult;
    private final Method method;
    private final Class<?> testClass;
    private final TestType testType;

    /**
     * Enum to clearly identify the type of test context
     */
    public enum TestType {
        REGULAR_TEST,    // Test executed through TestNG engine
        DISABLED_TEST    // Test marked with @Test(enabled = false)
    }

    /**
     * Constructor for regular tests that have been executed by TestNG.
     * 
     * @param testResult ITestResult containing test execution data
     */
    public TestNGTestWrapper(ITestResult testResult) {
        this.testResult = testResult;
        this.method = null;
        this.testClass = null;
        this.testType = TestType.REGULAR_TEST;
    }

    /**
     * Constructor for disabled tests that are discovered via reflection.
     * 
     * @param method The test method marked with @Test(enabled = false)
     * @param testClass The class containing the disabled test method
     */
    public TestNGTestWrapper(Method method, Class<?> testClass) {
        this.testResult = null;
        this.method = method;
        this.testClass = testClass;
        this.testType = TestType.DISABLED_TEST;
    }

    /**
     * Returns the ITestResult for regular tests.
     * 
     * @return ITestResult if this is a regular test, null for disabled tests
     */
    public ITestResult getTestResult() {
        return testResult;
    }

    /**
     * Returns the Method for disabled tests.
     * 
     * @return Method if this is a disabled test, null for regular tests
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the test class for disabled tests.
     * 
     * @return Class<?> if this is a disabled test, null for regular tests
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Returns the type of test context.
     * 
     * @return TestType enum indicating whether this is a regular or disabled test
     */
    public TestType getTestType() {
        return testType;
    }

    /**
     * Convenience method to check if this is a disabled test.
     * 
     * @return true if this context represents a disabled test
     */
    public boolean isDisabledTest() {
        return testType == TestType.DISABLED_TEST;
    }

    /**
     * Convenience method to check if this is a regular test.
     * 
     * @return true if this context represents a regular test
     */
    public boolean isRegularTest() {
        return testType == TestType.REGULAR_TEST;
    }

    /**
     * Returns a string representation for debugging purposes.
     */
    @Override
    public String toString() {
        if (isRegularTest()) {
            return String.format("TestNGTestContext{type=REGULAR_TEST, testClass=%s, method=%s}", 
                    testResult.getTestClass().getName(), 
                    testResult.getMethod().getMethodName());
        } else {
            return String.format("TestNGTestContext{type=DISABLED_TEST, testClass=%s, method=%s}", 
                    testClass.getSimpleName(), 
                    method.getName());
        }
    }

    /**
     * Factory method for creating context for regular tests.
     * 
     * @param testResult ITestResult from TestNG execution
     * @return TestNGTestContext for regular test
     */
    public static TestNGTestWrapper forRegularTest(ITestResult testResult) {
        return new TestNGTestWrapper(testResult);
    }

    /**
     * Factory method for creating context for disabled tests.
     * 
     * @param method The disabled test method
     * @param testClass The class containing the method
     * @return TestNGTestContext for disabled test
     */
    public static TestNGTestWrapper forDisabledTest(Method method, Class<?> testClass) {
        return new TestNGTestWrapper(method, testClass);
    }
}