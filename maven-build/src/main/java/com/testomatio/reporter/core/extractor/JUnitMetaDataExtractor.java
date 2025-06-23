package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.core.extractor.wrapper.JUnitTestWrapper;
import com.testomatio.reporter.logger.LoggerUtils;
import com.testomatio.reporter.model.TestMetadata;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JUnitMetaDataExtractor implements MetaDataExtractor<JUnitTestWrapper> {
    private final Logger LOGGER = LoggerUtils.getLogger(this.getClass());

    public TestMetadata extractTestMetadata(JUnitTestWrapper wrapper) {
        String title = getJUnitTestTitle(wrapper.getTestMethod(), wrapper.getExtensionContext());
        String suiteTitle = wrapper.getExtensionContext()
                .getTestClass()
                .map(Class::getSimpleName)
                .orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = getTestId(wrapper.getTestMethod());

        LOGGER.finer(String.format("Extracted test metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    private  String getJUnitTestTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
        LOGGER.finer(String.format("Using test title: %s (from %s)", title,
                titleAnnotation != null ? "@Title annotation" : "JUnit display name"));
        return title;
    }

    private static String getTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }
}
