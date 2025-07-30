package io.testomat.junit.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JunitMetaDataExtractor {

    public TestMetadata extractTestMetadata(ExtensionContext context) {
        Method testMethod = getTestMethod(context);
        String title = getTestTitle(testMethod);
        Class<?> testClass = context.getRequiredTestClass();
        String suiteTitle = testClass.getSimpleName();
        String file = getFilePath(testClass);
        String testId = getTestId(testMethod);

        return new TestMetadata(title, testId, suiteTitle, file);
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
     * Gets test title from @Title annotation or JUnit display name.
     */
    private String getTestTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    private String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(
                () -> new NoMethodInContextException(
                        "No test method found in " + context.getDisplayName()));
    }
}
