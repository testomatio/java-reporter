package com.testomatio.reporter.core.extractor.wrapper;

import java.lang.reflect.Method;
import lombok.Getter;
import org.testng.ITestResult;

/**
 * Wrapper for TestNG tests supporting both regular and disabled test contexts.
 */
@Getter
public class TestNGTestWrapper {
    private final ITestResult testResult;
    private final Method method;
    private final Class<?> testClass;
    private final TestType testType;

    public enum TestType {
        REGULAR_TEST,    // Executed test
        DISABLED_TEST    // @Test(enabled = false)
    }

    /**
     * Creates wrapper for regular executed test.
     */
    public TestNGTestWrapper(ITestResult testResult) {
        this.testResult = testResult;
        this.method = null;
        this.testClass = null;
        this.testType = TestType.REGULAR_TEST;
    }

    /**
     * Creates wrapper for disabled test discovered via reflection.
     */
    public TestNGTestWrapper(Method method, Class<?> testClass) {
        this.testResult = null;
        this.method = method;
        this.testClass = testClass;
        this.testType = TestType.DISABLED_TEST;
    }

    public boolean isRegularTest() {
        return testType == TestType.REGULAR_TEST;
    }

    @Override
    public String toString() {
        if (isRegularTest()) {
            assert testResult != null;
            return String.format("TestNGTestWrapper{type=REGULAR_TEST, testClass=%s, method=%s}",
                    testResult.getTestClass().getName(),
                    testResult.getMethod().getMethodName());
        } else {
            assert testClass != null;
            assert method != null;
            return String.format("TestNGTestWrapper{type=DISABLED_TEST, testClass=%s, method=%s}",
                    testClass.getSimpleName(),
                    method.getName());
        }
    }

    /**
     * Factory method for regular test.
     */
    public static TestNGTestWrapper forRegularTest(ITestResult testResult) {
        return new TestNGTestWrapper(testResult);
    }

    /**
     * Factory method for disabled test.
     */
    public static TestNGTestWrapper forDisabledTest(Method method, Class<?> testClass) {
        return new TestNGTestWrapper(method, testClass);
    }
}