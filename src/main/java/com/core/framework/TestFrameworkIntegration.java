package com.core.framework;

import com.model.TestResult;
import java.util.List;

public interface TestFrameworkIntegration {
    void initialize();
    void beforeTestSuite(String suiteName);
    void afterTestSuite(String suiteName);
    void beforeTest(String testName);
    void afterTest(TestResult result);
    void finalize();
    boolean isActive();
}