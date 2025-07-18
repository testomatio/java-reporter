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
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase()
            .contains("win");

    public String getTestClassFilePath(ExtensionContext extensionContext) {
        String foundFile = findTestFileByClassName(extensionContext);
        if (foundFile != null && !foundFile.equals(getDefaultPath(extensionContext))) {
            return foundFile;
        }

        try {
            String path = getPath(extensionContext);

            if (path != null) {
                String javaFilePath = convertClassPathToJavaFile(path, extensionContext);

                if (javaFilePath != null && Paths.get(javaFilePath).toFile().exists()) {
                    return javaFilePath;
                }
            }
        } catch (Exception e) {
            log.debug("Error getting test class file path: {}", e.getMessage(), e);
        }

        return foundFile;
    }

    private String convertClassPathToJavaFile(String classPath, ExtensionContext extensionContext) {
        try {
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String className = testClass.getName();
            String packagePath = className.replace(".", FILE_SEPARATOR);

            if (classPath.contains("target" + FILE_SEPARATOR + "test-classes")) {
                String projectRoot = classPath.substring(0, classPath.indexOf("target"
                        + FILE_SEPARATOR + "test-classes"));
                String javaFilePath = projectRoot + "src" + FILE_SEPARATOR + "test"
                        + FILE_SEPARATOR + "java" + FILE_SEPARATOR + packagePath + ".java";

                return normalizePath(javaFilePath);
            }

            if (classPath.endsWith(".class")) {
                String withoutExtension = classPath.substring(0, classPath.length() - 6);
                return withoutExtension + ".java";
            }

        } catch (Exception e) {
            // Exception handled silently
        }

        return null;
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
            String className = testClass.getName();
            String relativePath = className.replace(".", FILE_SEPARATOR) + ".java";

            List<String> possiblePaths = Arrays.asList(
                    "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java" + FILE_SEPARATOR
                            + relativePath,
                    ".." + FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR
                            + "java" + FILE_SEPARATOR + relativePath,
                    "test" + FILE_SEPARATOR + relativePath,
                    relativePath
            );

            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists()) {
                    String absolutePath = file.getAbsolutePath();
                    String normalizedPath = normalizePath(absolutePath);
                    return normalizedPath;
                }
            }

            String workingDir = System.getProperty("user.dir");

            List<String> workingDirPaths = Arrays.asList(
                    workingDir + FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR
                            + "java" + FILE_SEPARATOR + relativePath,
                    workingDir + FILE_SEPARATOR + ".." + FILE_SEPARATOR + "src" + FILE_SEPARATOR
                            + "test" + FILE_SEPARATOR + "java" + FILE_SEPARATOR + relativePath
            );

            for (String path : workingDirPaths) {
                File file = new File(path);
                if (file.exists()) {
                    String normalizedPath = normalizePath(file.getAbsolutePath());
                    return normalizedPath;
                }
            }

            return getDefaultPath(extensionContext);
        } catch (Exception e) {
            log.debug("Error finding test file by class name: {}", e.getMessage(), e);
            return getDefaultPath(extensionContext);
        }
    }

    private String getDefaultPath(ExtensionContext extensionContext) {
        try {
            Class<?> testClass = extensionContext.getRequiredTestClass();
            String relativePath = testClass.getName().replace(".", FILE_SEPARATOR) + ".java";
            String defaultPath = "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + relativePath;
            return defaultPath;
        } catch (Exception e) {
            String fallback = "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + "UnknownTest.java";
            return fallback;
        }
    }

    public String extractRelativeFilePath(String filepath) {
        try {
            if (filepath == null || filepath.isEmpty()) {
                String defaultPath = "src/test/java/UnknownFile.java";
                return defaultPath;
            }

            String normalizedPath = normalizePath(filepath);

            int srcIndex = normalizedPath.indexOf("src/");

            if (srcIndex != -1) {
                String result = normalizedPath.substring(srcIndex);
                return result;
            }

            return normalizedPath;
        } catch (Exception e) {
            log.debug("Error extracting relative file path: {}", e.getMessage(), e);
            String fallback = "src/test/java/UnknownFile.java";
            return fallback;
        }
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        try {
            Path normalizedPath = Paths.get(path).normalize();
            String result = normalizedPath.toString().replace('\\', '/');

            if (IS_WINDOWS) {
                result = normalizeWindowsPath(result);
            } else {
                result = normalizeUnixPath(result);
            }

            return result;
        } catch (Exception e) {
            log.debug("Error normalizing path '{}': {}", path, e.getMessage(), e);
            String fallback = path.replace('\\', '/');
            return fallback;
        }
    }

    private String normalizeWindowsPath(String path) {
        if (hasDriveLetter(path)) {
            return path;
        }

        if (path.startsWith("/") && path.length() > 1) {
            String result = path.substring(1);
            return result;
        }

        return path;
    }

    private String normalizeUnixPath(String path) {
        if (hasDriveLetter(path)) {
            if (path.startsWith("/")) {
                String result = path.substring(3);
                return result;
            } else {
                String result = path.substring(2);
                return result;
            }
        }

        return path;
    }

    private boolean hasDriveLetter(String path) {
        if (path.length() < 2) {
            return false;
        }

        int colonIndex = path.indexOf(':');
        if (colonIndex == -1) {
            return false;
        }

        if (path.startsWith("/") && colonIndex == 2) {
            char driveLetter = path.charAt(1);
            boolean isLetter = Character.isLetter(driveLetter);
            return isLetter;
        }

        if (colonIndex == 1) {
            char driveLetter = path.charAt(0);
            boolean isLetter = Character.isLetter(driveLetter);
            return isLetter;
        }

        return false;
    }
}
