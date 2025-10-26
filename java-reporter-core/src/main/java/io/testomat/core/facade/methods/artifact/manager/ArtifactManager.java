package io.testomat.core.facade.methods.artifact.manager;

/**
 * Manager for handling test artifacts with path validation and storage.
 * Provides secure file path validation and artifact registration for test runs.
 */

import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.exception.ArtifactManagementException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactManager {
    private static final Logger log = LoggerFactory.getLogger(ArtifactManager.class);

    public void storeDirectories(String... directories) {
        for (String dir : directories) {
            if (isValidFilePath(dir)) {
                TempArtifactDirectoriesStorage.store(dir);
            } else {
                log.info("Invalid artifact path provided: {}", dir);
            }
        }
    }

    private boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isRegularFile(path);
        } catch (InvalidPathException e) {
            log.warn("Provided filepath is not valid: {}", filePath);
            return false;
        } catch (Exception e) {
            throw new ArtifactManagementException("Unknown exception while file path validation", e);
        }
    }
}
