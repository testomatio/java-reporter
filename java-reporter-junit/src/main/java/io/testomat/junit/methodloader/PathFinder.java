package io.testomat.junit.methodloader;

import java.nio.file.Paths;
import org.junit.jupiter.api.extension.ExtensionContext;

public class PathFinder {

    public String getTestClassFilePath(ExtensionContext extensionContext) {
        try {
            String path = getPath(extensionContext);
            if (path != null) {
                path = path.replace('\\', '/');
                if (Paths.get(path).toFile().exists()) {
                    return path;
                }
            }
        } catch (Exception ignored) {
        }

        return findTestFileByClassName(extensionContext);
    }

    public String getPath(ExtensionContext context) {
        Class<?> testClass = context.getTestClass().orElseThrow(
                () -> new MethodLoaderException("No test class found"));

        return testClass.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();
    }

    private String findTestFileByClassName(ExtensionContext extensionContext) {
        try {
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String relativePath = testClass.getName().replace('.', '/') + ".java";

            String[] possiblePaths = {
                    "src/test/java/" + relativePath,
                    "test/" + relativePath,
                    relativePath
            };

            for (String path : possiblePaths) {
                if (Paths.get(path).toFile().exists()) {
                    return path;
                }
            }

            return "src/test/java/" + relativePath;
        } catch (Exception e) {
            return "src/test/java/UnknownTest.java";
        }
    }

    public String extractRelativeFilePath(String filepath) {
        try {
            String normalizedPath = filepath.replace('\\', '/');

            if (normalizedPath.length() > 2 && normalizedPath.charAt(1) == ':') {
                normalizedPath = normalizedPath.substring(2);
            }

            if (normalizedPath.startsWith("/")) {
                normalizedPath = normalizedPath.substring(1);
            }

            int srcIndex = normalizedPath.indexOf("src/");
            if (srcIndex != -1) {
                return normalizedPath.substring(srcIndex);
            }

            return normalizedPath.isEmpty() ? "src/test/java/UnknownFile.java" : normalizedPath;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
