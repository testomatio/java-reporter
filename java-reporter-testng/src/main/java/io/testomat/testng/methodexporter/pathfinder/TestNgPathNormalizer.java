package io.testomat.testng.methodexporter.pathfinder;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNgPathNormalizer {
    private static final Logger log = LoggerFactory.getLogger(TestNgPathNormalizer.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase()
            .contains("win");

    public String normalizePath(String path) {
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
            return path.replace('\\', '/');
        }
    }

    private String normalizeWindowsPath(String path) {
        if (hasDriveLetter(path)) {
            return path;
        }

        if (path.startsWith("/") && path.length() > 1) {
            return path.substring(1);
        }

        return path;
    }

    private String normalizeUnixPath(String path) {
        if (hasDriveLetter(path)) {
            if (path.startsWith("/")) {
                return path.substring(3);
            } else {
                return path.substring(2);
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
            return Character.isLetter(driveLetter);
        }

        if (colonIndex == 1) {
            char driveLetter = path.charAt(0);
            return Character.isLetter(driveLetter);
        }

        return false;
    }
}
