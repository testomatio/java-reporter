package com.testomatio.reporter.core.util;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testng.ITestResult;

import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

public class TestRunMetaDataExtractorUtil {

    public static TestMetadata extractTestMetadata(Method testMethod, ExtensionContext context) {
        String title = getJUnitTestTitle(testMethod, context);
        String suiteTitle = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(testMethod);

        getLogger(TestRunMetaDataExtractorUtil.class).finer(String.format("Extracted test metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    public static TestMetadata extractTestNGTestMetadata(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String title = getTestNGTestTitle(method, result);
        String testId = getTestId(method);
        String suiteTitle = result.getTestClass().getName();
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts test metadata for disabled TestNG tests.
     * Used when ITestResult is not available for disabled tests.
     *
     * @param method    the test method
     * @param testClass the class containing the test method
     * @return TestMetadata object with extracted information
     */
    public static TestMetadata extractTestMetadataForDisabledTest(Method method, Class<?> testClass) {
        String title = getTestTitle(method);
        String testId = getTestId(method);
        String suiteTitle = testClass.getSimpleName();
        String file = suiteTitle + ".java";

        getLogger(TestRunMetaDataExtractorUtil.class).finer(String.format("Extracted disabled test metadata - Title: %s, ID: %s, Suite: %s, File: %s", title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts test ID from method annotation.
     * Used by both JUnit and TestNG extractors.
     *
     * @param method the test method to extract ID from
     * @return test ID from @TestId annotation, or null if not present
     */
    public static String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    /**
     * Extracts test title from method annotation or method name.
     * Used for disabled TestNG tests where ITestResult is not available.
     *
     * @param method the test method to extract title from
     * @return test title from @Title annotation, or method name if not present
     */
    public static String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    private static String getTestNGTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }

    private static String getJUnitTestTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
        getLogger(TestRunMetaDataExtractorUtil.class).finer(String.format("Using test title: %s (from %s)", title,
                titleAnnotation != null ? "@Title annotation" : "JUnit display name"));
        return title;
    }
}