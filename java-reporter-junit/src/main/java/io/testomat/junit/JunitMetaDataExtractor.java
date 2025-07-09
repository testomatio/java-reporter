package io.testomat.junit;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JunitMetaDataExtractor {

    public TestMetadata extractTestMetadata(ExtensionContext context) {
        Method testMethod = getTestMethod(context);
        String title = getJUnitTestTitle(context);
        String suiteTitle = context
                .getTestClass()
                .map(Class::getSimpleName)
                .orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(testMethod);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Gets test title from @Title annotation or JUnit display name.
     */
    private String getJUnitTestTitle(ExtensionContext context) {
        Title titleAnnotation = getTestMethod(context).getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();

        return title;
    }

    /**
     * Gets test ID from @TestId annotation.
     */
    static String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(
                () -> new NoMethodInContextException(
                        "No test method found in " + context.getDisplayName()));
    }
}
