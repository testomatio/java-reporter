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
public class TestNgMetaDataExtractor implements MetaDataExtractor<TestNgTestWrapper> {

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
        String title = getTestTitle(method);
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
