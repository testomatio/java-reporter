package com.testomatio.reporter.core.extractor.wrapper;

import java.lang.reflect.Method;
import lombok.Getter;
import org.testng.ITestResult;

/**
 * Wrapper for TestNG tests supporting both regular and disabled test contexts.
 */
@Getter
public class TestNgTestWrapper {
    private final ITestResult testResult;
    private final Method method;
    private final Class<?> testClass;
    private final TestType testType;

    public enum TestType {
        REGULAR_TEST,
        DISABLED_TEST
    }

    /**
     * Creates wrapper for regular executed test.
     */
    public TestNgTestWrapper(ITestResult testResult) {
        this.testResult = testResult;
        this.method = null;
        this.testClass = null;
        this.testType = TestType.REGULAR_TEST;
    }

    /**
     * Creates wrapper for disabled test discovered via reflection.
     */
    public TestNgTestWrapper(Method method, Class<?> testClass) {
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
    public static TestNgTestWrapper forRegularTest(ITestResult testResult) {
        return new TestNgTestWrapper(testResult);
    }

    /**
     * Factory method for disabled test.
     */
    public static TestNgTestWrapper forDisabledTest(Method method, Class<?> testClass) {
        return new TestNgTestWrapper(method, testClass);
    }
}
