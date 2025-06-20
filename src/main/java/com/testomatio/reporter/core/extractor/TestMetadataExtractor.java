package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCase;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testng.ITestResult;

import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

/**
 * Service class responsible for extracting test metadata from different test frameworks.
 * Replaces static utility methods with instance-based approach for better testability
 * and dependency injection support.
 */
public class TestMetadataExtractor {
    private static final Logger LOGGER = getLogger(TestMetadataExtractor.class);
    
    private static final String UNKNOWN_SUITE = "Unknown Suite";
    private static final String UNKNOWN_FILE = "unknown.feature";
    private static final String UNKNOWN_TEST = "Unknown Test";
    private static final String TESTID_REGEX = "@T[a-z0-9]{8}";
    private static final String TITLE_PREFIX = "@title:";

    /**
     * Extracts metadata from JUnit 5 test method and context.
     */
    public TestMetadata extractFromJUnit(Method testMethod, ExtensionContext context) {
        String title = extractJUnitTitle(testMethod, context);
        String suiteTitle = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
        String file = suiteTitle + ".java";
        String testId = extractTestId(testMethod);

        LOGGER.finer(String.format("Extracted JUnit metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts metadata from TestNG test result.
     */
    public TestMetadata extractFromTestNG(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String title = extractTestNGTitle(method, result);
        String testId = extractTestId(method);
        String suiteTitle = result.getTestClass().getName();
        String file = suiteTitle + ".java";

        LOGGER.finer(String.format("Extracted TestNG metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts metadata from TestNG disabled test method.
     */
    public TestMetadata extractFromTestNGDisabled(Method method, Class<?> testClass) {
        String title = extractTitle(method);
        String testId = extractTestId(method);
        String suiteTitle = testClass.getSimpleName();
        String file = suiteTitle + ".java";

        LOGGER.finer(String.format("Extracted TestNG disabled metadata - Title: %s, ID: %s, Suite: %s, File: %s", 
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts metadata from Cucumber test case.
     */
    public TestMetadata extractFromCucumber(TestCase testCase) {
        String title = extractCucumberTitle(testCase);
        String testId = extractCucumberTestId(testCase);
        String suiteTitle = extractCucumberSuiteTitle(testCase);
        String file = extractCucumberFileName(testCase);

        LOGGER.finer(String.format("Extracted Cucumber metadata - Title: %s, ID: %s, Suite: %s, File: %s",
                title, testId, suiteTitle, file));

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    private String extractJUnitTitle(Method testMethod, ExtensionContext context) {
        Title titleAnnotation = testMethod.getAnnotation(Title.class);
        String title = titleAnnotation != null ? titleAnnotation.value() : context.getDisplayName();
        LOGGER.finer(String.format("Using JUnit title: %s (from %s)", title,
                titleAnnotation != null ? "@Title annotation" : "JUnit display name"));
        return title;
    }

    private String extractTestNGTitle(Method method, ITestResult result) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : result.getName();
    }

    private String extractTitle(Method method) {
        Title titleAnnotation = method.getAnnotation(Title.class);
        return titleAnnotation != null ? titleAnnotation.value() : method.getName();
    }

    private String extractTestId(Method method) {
        TestId testIdAnnotation = method.getAnnotation(TestId.class);
        return testIdAnnotation != null ? testIdAnnotation.value() : null;
    }

    private String extractCucumberTitle(TestCase testCase) {
        if (testCase == null) {
            return UNKNOWN_TEST;
        }

        Optional<String> titleFromTag = extractCucumberTitleFromTags(testCase)
                .map(tag -> tag.replace("_", " "));

        return titleFromTag.orElse(
                Optional.ofNullable(testCase.getName()).orElse(UNKNOWN_TEST)
        );
    }

    private String extractCucumberTestId(TestCase testCase) {
        if (testCase == null || testCase.getTags() == null) {
            return null;
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.matches(TESTID_REGEX))
                .findFirst()
                .orElse(null);
    }

    private String extractCucumberSuiteTitle(TestCase testCase) {
        return extractFileNameFromUri(testCase)
                .map(this::removeFileExtension)
                .orElse(UNKNOWN_SUITE);
    }

    private String extractCucumberFileName(TestCase testCase) {
        return extractFileNameFromUri(testCase).orElse(UNKNOWN_FILE);
    }

    private Optional<String> extractCucumberTitleFromTags(TestCase testCase) {
        if (testCase == null || testCase.getTags() == null) {
            return Optional.empty();
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.toLowerCase().startsWith(TITLE_PREFIX))
                .map(tag -> tag.substring(TITLE_PREFIX.length()))
                .findFirst();
    }

    private Optional<String> extractFileNameFromUri(TestCase testCase) {
        if (testCase == null || testCase.getUri() == null) {
            return Optional.empty();
        }

        return extractUriPath(testCase.getUri())
                .map(this::extractFileNameFromPath)
                .filter(name -> !name.isEmpty());
    }

    private Optional<String> extractUriPath(URI uri) {
        String path = safeGet(uri::getPath);
        if (isValidPath(path)) {
            return Optional.of(cleanUriPath(path));
        }

        path = safeGet(uri::toString);
        if (isValidPath(path)) {
            return Optional.of(cleanUriPath(path));
        }

        path = safeGet(uri::getSchemeSpecificPart);
        if (isValidPath(path)) {
            return Optional.of(cleanUriPath(path));
        }

        return Optional.empty();
    }

    private String cleanUriPath(String path) {
        if (path.contains(":") && !path.startsWith("/")) {
            int colonIndex = path.indexOf(":");
            if (colonIndex < path.length() - 1) {
                return path.substring(colonIndex + 1);
            }
        }
        return path;
    }

    private String extractFileNameFromPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    private String removeFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot != -1 ? fileName.substring(0, lastDot) : fileName;
    }

    private String safeGet(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidPath(String path) {
        return path != null && !path.isEmpty();
    }
}