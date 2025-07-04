package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.extractor.wrapper.TestNgTestWrapper;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.testng.ITestResult;

/**
 * Extracts test metadata from TestNG test methods and results.
 * Supports both regular and disabled tests with @Title and @TestId annotations.
 */
public class TestNgMetaDataExtractor implements MetaDataExtractor<TestNgTestWrapper> {
    private static final Logger LOGGER = LoggerUtils.getLogger(TestNgMetaDataExtractor.class);

    @Override
    public TestMetadata extractTestMetadata(TestNgTestWrapper wrapper) {
        if (wrapper.isRegularTest()) {
            return extractTestMetadataForRegularTest(wrapper.getTestResult());
        } else {
            return extractTestMetadataForDisabledTest(wrapper.getMethod(), wrapper.getTestClass());
        }
    }

    /**
     * Extracts metadata from executed TestNG test result.
     */
    private TestMetadata extractTestMetadataForRegularTest(ITestResult source) {
        Method method = source.getMethod().getConstructorOrMethod().getMethod();
        String title = getTestNgTestTitle(method, source);
        String testId = getTestId(method);
        String suiteTitle = source.getTestClass().getName();
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts metadata from disabled test method via reflection.
     */
    private TestMetadata extractTestMetadataForDisabledTest(Method method, Class<?> testClass) {
        String title = getTestTitle(method);
        String testId = getTestId(method);
        String suiteTitle = testClass.getSimpleName();
        String file = suiteTitle + ".java";

        LOGGER.finer(String.format(
                "Extracted disabled test metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Gets test ID from @TestId annotation.
     */
    private String getTestId(Method method) {
        return JunitMetaDataExtractor.getTestId(method);
    }

    /**
     * Gets test title from @Title annotation or method name.
     */
    private String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    /**
     * Gets test title from @Title annotation or TestNG result name.
     */
    private String getTestNgTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }
}
