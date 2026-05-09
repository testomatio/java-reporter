package io.testomat.testng.extractor;

import io.testomat.core.annotation.LinkTest;
import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.model.Link;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        Class<?> testClass;

        if (wrapper.isRegularTest()) {
            ITestResult testResult = wrapper.getTestResult();
            method = testResult.getMethod().getConstructorOrMethod().getMethod();
            testClass = testResult.getTestClass().getRealClass();
        } else {
            method = wrapper.getMethod();
            testClass = wrapper.getTestClass();
        }
        suiteTitle = testClass.getSimpleName();

        String title = getTestTitle(method);
        String testId = getTestId(method);
        String filePath = getFilePath(testClass);
        List<Link> link = getLinks(method);

        return new TestMetadata(title, testId, suiteTitle, filePath, link);
    }

    /**
     * Gets the file path for the test class.
     *
     * @param testClass the test class
     * @return path to the java file
     */
    private String getFilePath(Class<?> testClass) {
        String packagePath = testClass.getPackage().getName().replace('.', '/');
        String className = testClass.getSimpleName() + ".java";
        return packagePath + "/" + className;
    }

    /**
     * Gets test ID from @TestId annotation.
     */
    public String getTestId(Method method) {
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

    /**
     * Gets test ids from @LinkTest annotation.
     */
    private List<Link> getLinks(Method method) {
        LinkTest annotation = method.getAnnotation(LinkTest.class);

        if (annotation == null || annotation.value().length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(annotation.value())
            .map(Link::test)
            .collect(Collectors.toList());
    }
}
