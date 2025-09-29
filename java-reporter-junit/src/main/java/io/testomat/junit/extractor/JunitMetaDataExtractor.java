package io.testomat.junit.extractor;

import io.testomat.core.annotation.TestId;
import io.testomat.core.annotation.Title;
import io.testomat.core.exception.NoMethodInContextException;
import io.testomat.core.model.TestMetadata;
import io.testomat.junit.extractor.strategy.ParameterExtractorService;
import io.testomat.junit.util.ParameterizedTestSupport;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extracts metadata and parameters from JUnit test methods.
 * Provides comprehensive test information including titles, IDs, suite names,
 * file paths, and parameter extraction for parameterized tests.
 */
public class JunitMetaDataExtractor {

    private final ParameterExtractorService parameterExtractorService;

    public JunitMetaDataExtractor() {
        this.parameterExtractorService = new ParameterExtractorService();
    }

    /**
     * Constructor for testing with custom parameter extractor service.
     *
     * @param parameterExtractorService the custom parameter extractor service
     */
    public JunitMetaDataExtractor(ParameterExtractorService parameterExtractorService) {
        this.parameterExtractorService = parameterExtractorService;
    }

    /**
     * Extracts complete metadata for a test method.
     *
     * @param context the JUnit extension context
     * @return TestMetadata containing title, ID, suite name, and file path
     * @throws NoMethodInContextException if no test method is found
     */
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

    /**
     * Extracts parameters from parameterized tests.
     *
     * @param context the JUnit extension context
     * @return parameter object (single value for simple params, Map for multiple/complex params),
     * or null for non-parameterized tests
     */
    public Object extractTestParameters(ExtensionContext context) {
        return parameterExtractorService.extractParameters(context);
    }

    /**
     * Checks if the test method is a parameterized test.
     *
     * @param context the JUnit extension context
     * @return true if the test method is annotated with @ParameterizedTest
     */
    public boolean isParameterizedTest(ExtensionContext context) {
        try {
            Method method = context.getTestMethod().orElse(null);
            return ParameterizedTestSupport.isParameterizedTest(method);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildTestTitle(Method method, ExtensionContext context) {
        try {
            String titleFromAnnotation = extractTitleFromAnnotation(method);
            if (!titleFromAnnotation.equals(method.getName())) {
                return titleFromAnnotation;
            }

            // Check for ParameterizedTest annotation using reflection
            if (ParameterizedTestSupport.isParameterizedTest(method)) {
                ParameterizedTestSupport.loadAnnotationClass("ParameterizedTest")
                        .ifPresent(parameterizedTestClass -> {
                            try {
                                Object parameterized = method.getAnnotation(parameterizedTestClass);
                                if (parameterized != null) {
                                    buildParameterizedTitle(method, parameterized);
                                }
                            } catch (Exception e) {
                                // Ignore reflection errors
                            }
                        });
            }

            return method.getName();
        } catch (Exception e) {
            return context.getDisplayName();
        }
    }

    private String buildParameterizedTitle(Method method, Object parameterized) {
        try {
            // Use reflection to safely access the name() method
            java.lang.reflect.Method nameMethod = parameterized.getClass().getMethod("name");
            String customName = (String) nameMethod.invoke(parameterized);
            if (isCustomNameProvided(customName)) {
                return method.getName();
            }
            return method.getName();
        } catch (Exception e) {
            return method.getName();
        }
    }

    private boolean isCustomNameProvided(String name) {
        return name != null
                && !name.trim().isEmpty()
                && !name.equals("{default_display_name}");
    }

    private String extractTitleFromAnnotation(Method method) {
        Title annotation = method.getAnnotation(Title.class);
        return annotation != null ? annotation.value() : method.getName();
    }

    public static String extractTestId(Method method) {
        TestId annotation = method.getAnnotation(TestId.class);
        return annotation != null ? annotation.value() : null;
    }

    private String buildFilePath(Class<?> testClass) {
        String packagePath = testClass.getPackage().getName().replace('.', '/');
        return packagePath + "/" + testClass.getSimpleName() + ".java";
    }

    private Method getTestMethod(ExtensionContext context) {
        return context.getTestMethod().orElseThrow(() ->
                new NoMethodInContextException("No test method found in "
                        + context.getDisplayName())
        );
    }
}
