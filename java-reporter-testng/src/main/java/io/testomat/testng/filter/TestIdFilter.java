package io.testomat.testng.filter;

import io.testomat.core.annotation.TestId;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Filter for TestNG tests based on @TestId annotation.
 * Provides filtering logic to include only tests with matching IDs.
 */
public class TestIdFilter {

    /**
     * Checks if test should be executed based on provided filter IDs.
     * Throws SkipException if test should be skipped.
     *
     * @param result test result containing method information
     * @throws SkipException if test should be filtered out
     */
    public void filterTest(ITestResult result) {
        String providedIds = System.getProperty("ids");
        if (providedIds == null || providedIds.trim().isEmpty()) {
            return;
        }

        TestId testIdAnnotation = result.getMethod().getConstructorOrMethod()
                .getMethod().getAnnotation(TestId.class);

        if (testIdAnnotation == null) {
            throw new SkipException("Test filtered: no @TestId annotation");
        }

        String testId = testIdAnnotation.value();
        if (!isTestIdIncluded(testId, providedIds)) {
            throw new SkipException("Test filtered by ID: " + testId);
        }
    }

    private boolean isTestIdIncluded(String testId, String providedIds) {
        if (testId == null || providedIds == null) {
            return false;
        }

        String[] allowedIds = providedIds.split(",");
        for (String allowedId : allowedIds) {
            String trimmed = allowedId.trim();
            if (!trimmed.isEmpty() && testId.equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
}
