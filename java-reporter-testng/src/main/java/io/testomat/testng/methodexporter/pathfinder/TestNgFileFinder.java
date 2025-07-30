package io.testomat.testng.methodexporter.pathfinder;

import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNgFileFinder {
    private static final Logger log = LoggerFactory.getLogger(TestNgFileFinder.class);
    private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();
    private final TestNgPathNormalizer normalizer;

    public TestNgFileFinder() {
        normalizer = new TestNgPathNormalizer();
    }

    /**
     * Constructor for testing
     */
    public TestNgFileFinder(final TestNgPathNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    /**
     * Finds the source file path for the specified test class.
     */
    public String getTestClassFilePath(Class<?> testClass) {
        String foundFile = findTestFileByClassName(testClass);
        if (foundFile != null && !foundFile.equals(getDefaultPath(testClass))) {
            return foundFile;
        }

        try {
            String path = getPath(testClass);

            if (path != null) {
                String javaFilePath = convertClassPathToJavaFile(path, testClass);

                if (javaFilePath != null && Paths.get(javaFilePath).toFile().exists()) {
                    return javaFilePath;
                }
            }
        } catch (Exception e) {
            log.debug("Error getting test class file path: {}", e.getMessage(), e);
        }

        return foundFile;
    }

    private String convertClassPathToJavaFile(String classPath, Class<?> testClass) {
        String className = testClass.getName();
        String packagePath = className.replace(".", FILE_SEPARATOR);

        if (classPath.contains("target" + FILE_SEPARATOR + "test-classes")) {
            String projectRoot = classPath.substring(0, classPath.indexOf("target"
                    + FILE_SEPARATOR + "test-classes"));
            String javaFilePath = projectRoot + "src" + FILE_SEPARATOR + "test"
                    + FILE_SEPARATOR + "java" + FILE_SEPARATOR + packagePath + ".java";

            return normalizer.normalizePath(javaFilePath);
        }

        if (classPath.endsWith(".class")) {
            String withoutExtension = classPath.substring(0, classPath.length() - 6);
            return withoutExtension + ".java";
        }
        return null;
    }

    /**
     * Gets the location path from the test class.
     */
    public String getPath(Class<?> testClass) {
        try {
            URI uri = testClass.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            return URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Error getting path from test class: {}", e.getMessage(), e);
            return null;
        }
    }

    private String findTestFileByClassName(Class<?> testClass) {
        try {
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
                    return normalizer.normalizePath(absolutePath);
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
                    return normalizer.normalizePath(file.getAbsolutePath());
                }
            }

            return getDefaultPath(testClass);
        } catch (Exception e) {
            log.debug("Error finding test file by class name: {}", e.getMessage(), e);
            return getDefaultPath(testClass);
        }
    }

    private String getDefaultPath(Class<?> testClass) {
        try {
            String relativePath = testClass.getName().replace(".", FILE_SEPARATOR) + ".java";
            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + relativePath;
        } catch (Exception e) {
            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + "UnknownTest.java";
        }
    }

    /**
     * Extracts package-relative file path from full file path.
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
