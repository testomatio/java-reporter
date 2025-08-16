package io.testomat.junit.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

public class JunitMetaDataExtractor {

    public TestMetadata extractTestMetadata(ExtensionContext context) {
        Method method = getTestMethod(context);
        Class<?> testClass = context.getRequiredTestClass();
        
        return new TestMetadata(
            buildTestTitle(method, context),
            extractTestId(method),
            testClass.getSimpleName(),
            buildFilePath(testClass)
        );
    }

    public Object extractTestParameters(ExtensionContext context) {
        return ParameterExtractor.extractTestParameters(context);
    }

    public boolean isParameterizedTest(ExtensionContext context) {
        return ParameterExtractor.isParameterizedTest(context);
    }

    private String buildTestTitle(Method method, ExtensionContext context) {
        try {
            String titleFromAnnotation = extractTitleFromAnnotation(method);
            if (!titleFromAnnotation.equals(method.getName())) {
                return titleFromAnnotation;
            }
            
            ParameterizedTest parameterized = method.getAnnotation(ParameterizedTest.class);
            if (parameterized != null) {
                return buildParameterizedTitle(method, parameterized);
            }
            
            return method.getName();
        } catch (Exception e) {
            return context.getDisplayName();
        }
    }

    private String buildParameterizedTitle(Method method, ParameterizedTest parameterized) {
        String customName = parameterized.name();
        if (isCustomNameProvided(customName)) {
            return method.getName() + " |>" + customName;
        }
        return method.getName();
    }

    private boolean isCustomNameProvided(String name) {
        return name != null && 
               !name.trim().isEmpty() && 
               !name.equals("{default_display_name}");
    }

    private String extractTitleFromAnnotation(Method method) {
        Title annotation = method.getAnnotation(Title.class);
        return annotation != null ? annotation.value() : method.getName();
    }

    private String extractTestId(Method method) {
        TestId annotation = method.getAnnotation(TestId.class);
        return annotation != null ? annotation.value() : null;
    }

    private String buildFilePath(Class<?> testClass) {
        String packagePath = testClass.getPackage().getName().replace('.', '/');
        return packagePath + "/" + testClass.getSimpleName() + ".java";
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(() -> 
            new NoMethodInContextException("No test method found in " + context.getDisplayName())
        );
    }
}