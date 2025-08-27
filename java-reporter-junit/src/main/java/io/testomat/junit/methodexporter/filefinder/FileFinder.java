package io.testomat.junit.methodexporter.filefinder;

import io.testomat.junit.exception.MethodExporterException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locates source files for test classes in various common directory structures.
 * This class searches through standard Maven/Gradle directory layouts to find
 * the corresponding .java source file for a given test class.
 */
public class FileFinder {
    private static final Logger log = LoggerFactory.getLogger(FileFinder.class);
    private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();
    private final PathNormalizer normalizer;

    public FileFinder() {
        normalizer = new PathNormalizer();
    }

    /**
     * Constructor for testing
     *
     * @param normalizer the path normalizer
     */
    public FileFinder(final PathNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * Finds the source file path for the given test class.
     * Searches through common source directory structures including Maven and Gradle layouts.
     *
     * @param testClass the test class to locate
     * @return the file path to the source file, or null if not found
     */
    public String getTestClassFilePath(Class<?> testClass) {
        if (testClass == null) {
            return null;
        }

        String className = testClass.getName();
        String packagePath = className.replace('.', '/');
        String fileName = packagePath + ".java";

        String[] sourceDirs = {
                "src/test/java/",
                "src/main/java/",
                "test/",
                "src/"
        };

        for (String sourceDir : sourceDirs) {
            String fullPath = sourceDir + fileName;
            if (new java.io.File(fullPath).exists()) {
                return fullPath;
            }
        }

        return "src/test/java/" + fileName;
    }

    public String getPath(ExtensionContext context) {
        try {
            Class<?> testClass = context.getTestClass().orElseThrow(
                    () -> new MethodExporterException("No test class found"));

            URI uri = testClass.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            return URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Error getting path from context: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts package-relative file path from full file path.
     * Updated to work similarly to TestNgFileFinder but for JUnit context.
     *
     * @param filepath the full file path to process
     * @return the relative file path from the package root,
     * or "UnknownFile.java" if path is invalid
     */
    public String extractRelativeFilePath(String filepath) {
        try {
            if (filepath == null || filepath.isEmpty()) {
                return "UnknownFile.java";
            }

            String normalizedPath = normalizer.normalizePath(filepath);

            if (normalizedPath.contains("src/test/java/")) {
                int index = normalizedPath.indexOf("src/test/java/");
                return normalizedPath.substring(index + "src/test/java/".length());
            }

            if (normalizedPath.contains("src/main/java/")) {
                int index = normalizedPath.indexOf("src/main/java/");
                return normalizedPath.substring(index + "src/main/java/".length());
            }

            if (normalizedPath.contains("src/") && normalizedPath.contains("/java/")) {
                int javaIndex = normalizedPath.lastIndexOf("/java/");
                if (javaIndex != -1) {
                    return normalizedPath.substring(javaIndex + "/java/".length());
                }
            }

            if (!normalizedPath.contains("/")
                    || normalizedPath.matches("^[a-zA-Z0-9._/]+\\.java$")) {
                return normalizedPath;
            }

            return normalizedPath;
        } catch (Exception e) {
            log.debug("Error extracting relative file path: {}", e.getMessage(), e);
            return "UnknownFile.java";
        }
    }
}
