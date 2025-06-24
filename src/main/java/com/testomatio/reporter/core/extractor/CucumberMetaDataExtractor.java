package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCase;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Extracts test metadata from Cucumber test cases.
 * Supports @title: tags for custom titles and @T[8chars] tags for test IDs.
 */
public class CucumberMetaDataExtractor implements MetaDataExtractor<TestCase> {

    private static final String TEST_ID_REGEX = "@T[a-z0-9]{8}";
    private static final String TITLE_PREFIX = "@title:";

    protected static final String UNKNOWN_SUITE = "Unknown Suite";
    protected static final String UNKNOWN_FILE = "unknown";
    protected static final String UNKNOWN_TEST = "Unknown Test";

    @Override
    public TestMetadata extractTestMetadata(TestCase testCase) {
        String title = extractTitle(testCase);
        String testId = extractTestId(testCase);
        String suiteTitle = extractSuiteTitle(testCase);
        String file = extractFileName(testCase);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    /**
     * Extracts test title from @title: tag or falls back to test case name.
     */
    private String extractTitle(TestCase testCase) {
        if (testCase == null) {
            return UNKNOWN_TEST;
        }

        Optional<String> titleFromTag = extractFromTags(testCase)
                .map(tag -> tag.replace("_", " "));

        return titleFromTag.orElse(
                safeGetString(testCase.getName())
        );
    }

    /**
     * Extracts test ID from @T[8chars] tag format.
     */
    private String extractTestId(TestCase testCase) {
        if (testCase == null || testCase.getTags() == null) {
            return null;
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.matches(TEST_ID_REGEX))
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts suite title from feature file name without extension.
     */
    private String extractSuiteTitle(TestCase testCase) {
        return extractFileNameFromUri(testCase)
                .map(this::removeFileExtension)
                .orElse(UNKNOWN_SUITE);
    }

    /**
     * Extracts feature file name from test case URI.
     */
    private String extractFileName(TestCase testCase) {
        return extractFileNameFromUri(testCase).orElse(UNKNOWN_FILE + ".feature");
    }

    private Optional<String> extractFromTags(TestCase testCase) {
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

    private Optional<String> extractUriPath(java.net.URI uri) {
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

    private String safeGetString(Object obj) {
        return obj != null ? obj.toString() : CucumberMetaDataExtractor.UNKNOWN_TEST;
    }
}