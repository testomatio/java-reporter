package io.testomat.junit.methodexporter;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinder {
    private static final Logger log = LoggerFactory.getLogger(PathFinder.class);
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public String getTestClassFilePath(ExtensionContext extensionContext) {
        try {
            String path = getPath(extensionContext);
            if (path != null) {
                String normalizedPath = normalizePath(path);
                if (Paths.get(normalizedPath).toFile().exists()) {
                    return normalizedPath;
                }
            }
        } catch (Exception e) {
            log.debug("Error getting test class file path: {}", e.getMessage(), e);
        }

        return findTestFileByClassName(extensionContext);
    }

    public String getPath(ExtensionContext context) {
        try {
            Class<?> testClass = context.getTestClass().orElseThrow(
                    () -> new MethodExporterException("No test class found"));

            URI uri = testClass.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            String decodedPath = URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8.name());
            return decodedPath;
        } catch (Exception e) {
            log.debug("Error getting path from context: {}", e.getMessage(), e);
            return null;
        }
    }

    private String findTestFileByClassName(ExtensionContext extensionContext) {
        try {
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String relativePath = testClass.getName().replace(".", FILE_SEPARATOR) + ".java";

            List<String> possiblePaths = Arrays.asList(
                    "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java" + FILE_SEPARATOR + relativePath,
                    "test" + FILE_SEPARATOR + relativePath,
                    relativePath
            );

            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists()) {
                    return normalizePath(file.getAbsolutePath());
                }
            }

            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java" + FILE_SEPARATOR + relativePath;
        } catch (Exception e) {
            log.debug("Error finding test file by class name: {}", e.getMessage(), e);
            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java" + FILE_SEPARATOR + "UnknownTest.java";
        }
    }

    public String extractRelativeFilePath(String filepath) {
        try {
            if (filepath == null || filepath.isEmpty()) {
                return "src/test/java/UnknownFile.java";
            }

            String normalizedPath = normalizePath(filepath);

            int srcIndex = normalizedPath.indexOf("src/");
            if (srcIndex != -1) {
                return normalizedPath.substring(srcIndex);
            }

            return normalizedPath;
        } catch (Exception e) {
            log.debug("Error extracting relative file path: {}", e.getMessage(), e);
            return "src/test/java/UnknownFile.java";
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        try {
            Path normalizedPath = Paths.get(path).normalize();

            String result = normalizedPath.toString().replace('\\', '/');

            if (IS_WINDOWS && result.length() > 2 && result.charAt(1) == ':') {
                return result;
            } else if (!IS_WINDOWS && result.length() > 2 && result.charAt(1) == ':') {
                result = result.substring(2);
            }

            if (result.startsWith("/") && !result.startsWith("//")) {
                if (!IS_WINDOWS || !result.matches("^/[a-zA-Z]:/.*")) {
                    result = result.substring(1);
                }
            }

            return result;
        } catch (Exception e) {
            log.debug("Error normalizing path '{}': {}", path, e.getMessage(), e);
            return path.replace('\\', '/');
        }
    }
}