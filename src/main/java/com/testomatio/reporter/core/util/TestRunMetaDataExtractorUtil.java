package com.testomatio.reporter.core.util;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCase;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
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

    /**
     * Extracts test ID from Cucumber tags.
     * Looks for tags in format @TestId:value or @testid:value (case insensitive).
     *
     * @param testCase the Cucumber test case
     * @return test ID from tag, or null if not found
     */
    private static String getCucumberTestId(TestCase testCase) {
        Collection<String> tags = testCase.getTags();

        return tags.stream()
                .filter(tag -> tag.toLowerCase().startsWith("@testid:"))
                .map(tag -> tag.substring(8))
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts test title from Cucumber tags or uses scenario name as fallback.
     * Looks for tags in format @Title:value or @title:value (case insensitive).
     *
     * @param testCase the Cucumber test case
     * @return test title from tag or scenario name
     */
    private static String getCucumberTestTitle(TestCase testCase) {
        Collection<String> tags = testCase.getTags();

        Optional<String> titleFromTag = tags.stream()
                .filter(tag -> tag.toLowerCase().startsWith("@title:"))
                .map(tag -> tag.substring(7).replace("_", " "))
                .findFirst();

        String title = titleFromTag.orElse(testCase.getName());

        getLogger(TestRunMetaDataExtractorUtil.class).finer(String.format(
                "Using Cucumber test title: %s (from %s)",
                title, titleFromTag.isPresent() ? "@Title tag" : "scenario name"));

        return title;
    }

    /**
     * Extracts suite title from Cucumber feature name.
     *
     * @param testCase the Cucumber test case
     * @return feature name as suite title
     */
    private static String getCucumberSuiteTitle(TestCase testCase) {
        String featureName = testCase.getUri().getPath();

        // Extract just the filename without path and extension
        int lastSlash = featureName.lastIndexOf('/');
        if (lastSlash != -1) {
            featureName = featureName.substring(lastSlash + 1);
        }

        int lastDot = featureName.lastIndexOf('.');
        if (lastDot != -1) {
            featureName = featureName.substring(0, lastDot);
        }

        return featureName;
    }

    /**
     * Extracts file path from Cucumber test case URI.
     *
     * @param testCase the Cucumber test case
     * @return file path with .feature extension
     */
    private static String getCucumberFile(TestCase testCase) {
        String path = testCase.getUri().getPath();

        // Extract just the filename from full path
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            return path.substring(lastSlash + 1);
        }

        return path;
    }
}