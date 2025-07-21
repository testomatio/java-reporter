package io.testomat.junit.methodexporter.patfinder;

import io.testomat.junit.exception.MethodExporterException;
import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileFinder {
    private static final Logger log = LoggerFactory.getLogger(FileFinder.class);
    private static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();
    private final PathNormalizer normalizer;

    public FileFinder() {
        normalizer = new PathNormalizer();
    }

    /**
     * Constructor for testing
     */
    public FileFinder(final PathNormalizer normalizer) {
        this.normalizer = normalizer;
    }

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

                return normalizer.normalizePath(javaFilePath);
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

            return URLDecoder.decode(uri.getPath(), StandardCharsets.UTF_8);
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
            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + relativePath;
        } catch (Exception e) {
            return "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "java"
                    + FILE_SEPARATOR + "UnknownTest.java";
        }
    }

    public String extractRelativeFilePath(String filepath) {
        try {
            if (filepath == null || filepath.isEmpty()) {
                return "src/test/java/UnknownFile.java";
            }

            String normalizedPath = normalizer.normalizePath(filepath);

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
}
