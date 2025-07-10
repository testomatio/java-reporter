package io.testomat.testng.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import org.testng.ITestResult;

/**
 * Extracts test metadata from TestNG test methods and results.
 * Supports both regular and disabled tests with @Title and @TestId annotations.
 */
public class TestNgMetaDataExtractor {

    /**
     * Extracts test metadata from framework-specific test wrapper.
     *
     * @param wrapper TestNG test wrapper containing test data
     * @return extracted test metadata
     */
    public TestMetadata extractTestMetadata(TestNgTestWrapper wrapper) {
        Method method;
        String suiteTitle;

        if (wrapper.isRegularTest()) {
            ITestResult testResult = wrapper.getTestResult();
            method = testResult.getMethod().getConstructorOrMethod().getMethod();
            suiteTitle = testResult.getTestClass().getName();
        } else {
            method = wrapper.getMethod();
            suiteTitle = wrapper.getTestClass().getSimpleName();
        }

        String title = getTestTitle(method);
        String testId = getTestId(method);
        String file = suiteTitle + ".java";

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Gets test ID from @TestId annotation.
     */
    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    /**
     * Gets test title from @Title annotation or method name.
     */
    private String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }
}
