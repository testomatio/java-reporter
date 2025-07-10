package io.testomat.cucumber.extractor;

import io.cucumber.plugin.event.TestCase;
import io.testomat.core.model.TestMetadata;
import java.net.URI;

/**
 * Extracts test metadata from Cucumber test cases.
 * Supports @title: tags for custom titles and @T[8chars] tags for test IDs.
 */
public class CucumberMetaDataExtractor {
    private static final String TEST_ID_REGEX = "@T[a-z0-9]{8}";
    private static final String TITLE_PREFIX = "@title:";
    private static final String UNKNOWN_TEST = "Unknown test";
    private static final String UNKNOWN_FEATURE = "unknown.feature";

    public TestMetadata extractTestMetadata(TestCase testCase) {
        String title = extractTitle(testCase);
        String testId = extractTestId(testCase);
        String fileName = extractFileName(testCase);
        String suiteTitle = removeExtension(fileName);

        return new TestMetadata(title, testId, suiteTitle, fileName);
    }

    private String extractTitle(TestCase testCase) {
        if (testCase == null || testCase.getTags() == null) {
            return getTestName(testCase);
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.toLowerCase().startsWith(TITLE_PREFIX))
                .map(tag -> tag.substring(TITLE_PREFIX.length()).replace("_", " "))
                .findFirst()
                .orElse(getTestName(testCase));
    }

    private String extractTestId(TestCase testCase) {
        if (testCase == null || testCase.getTags() == null) {
            return null;
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.matches(TEST_ID_REGEX))
                .findFirst()
                .orElse(null);
    }

    private String extractFileName(TestCase testCase) {
        if (testCase == null || testCase.getUri() == null) {
            return UNKNOWN_FEATURE;
        }

        String fileName = getFileNameFromUri(testCase.getUri());
        return fileName != null ? fileName : UNKNOWN_FEATURE;
    }

    private String getFileNameFromUri(URI uri) {
        try {
            String path = uri.getPath();
            if (path == null) {
                path = uri.toString();
            }

            if (path.contains(":") && !path.startsWith("/")) {
                int colonIndex = path.indexOf(":");
                if (colonIndex < path.length() - 1) {
                    path = path.substring(colonIndex + 1);
                }
            }

            int lastSlash = path.lastIndexOf('/');
            return lastSlash != -1 ? path.substring(lastSlash + 1) : path;

        } catch (Exception e) {
            return null;
        }
    }

    private String removeExtension(String fileName) {
        if (fileName == null) {
            return "Unknown Suite";
        }

        int lastDot = fileName.lastIndexOf('.');
        return lastDot != -1 ? fileName.substring(0, lastDot) : fileName;
    }

    private String getTestName(TestCase testCase) {
        if (testCase == null) {
            return UNKNOWN_TEST;
        }

        try {
            return testCase.getName() != null ? testCase.getName() : UNKNOWN_TEST;
        } catch (Exception e) {
            return UNKNOWN_TEST;
        }
    }
}
