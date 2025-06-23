package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.extractor.wrapper.TestNGTestWrapper;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.testng.ITestResult;

public class TestNGMetaDataExtractor implements MetaDataExtractor<TestNGTestWrapper> {
    private final Logger LOGGER = LoggerUtils.getLogger(this.getClass());

    @Override
    public TestMetadata extractTestMetadata(TestNGTestWrapper wrapper) {
        if (wrapper.isRegularTest()) {
            return extractTestMetadataForRegularTest(wrapper.getTestResult());
        } else {
            return extractTestMetadataForDisabledTest(wrapper.getMethod(), wrapper.getTestClass());
        }
    }
    private TestMetadata extractTestMetadataForRegularTest(ITestResult source) {
        Method method = source.getMethod().getConstructorOrMethod().getMethod();
        String title = getTestNGTestTitle(method, source);
        String testId = getTestId(method);
        String suiteTitle = source.getTestClass().getName();
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts metadata for disabled tests discovered via reflection.
     *
     * @param method The disabled test method
     * @param testClass The class containing the disabled test method
     * @return TestMetadata for the disabled test
     */
    private TestMetadata extractTestMetadataForDisabledTest(Method method, Class<?> testClass) {
        String title = getTestTitle(method);
        String testId = getTestId(method);
        String suiteTitle = testClass.getSimpleName();
        String file = suiteTitle + ".java";

        LOGGER.finer(String.format("Extracted disabled test metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    private String getTestNGTestTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }
}