package com.testomatio.reporter.core.util;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

public class TestMetaDataExtractorUtil{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestMetaDataExtractorUtil.class);

    public static TestMetadata extractTestMetadata(Method testMethod, ExtensionContext context) {
        String title = getJUnitTestTitle(testMethod, context);
        String suiteTitle = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(testMethod);

        LOGGER.debug("Extracted test metadata - Title: {}, ID: {}, Suite: {}, File: {}",
                title, testId, suiteTitle, file);

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

    private static String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private static String getTestNGTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }

    private static String getJUnitTestTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
        LOGGER.debug("Using test title: {} (from {})", title,
                titleAnnotation != null ? "@Title annotation" : "JUnit display name");
        return title;
    }
}
