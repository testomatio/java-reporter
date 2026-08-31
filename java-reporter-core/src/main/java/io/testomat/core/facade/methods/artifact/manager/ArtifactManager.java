package io.testomat.core.facade.methods.artifact.manager;

/**
 * Manager for handling test artifacts with path validation and storage.
 * Provides secure file path validation and artifact registration for test runs.
 */

import io.testomat.core.exception.ArtifactManagementException;
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactManager {
    private static final Logger log = LoggerFactory.getLogger(ArtifactManager.class);

    public void storeDirectories(String... directories) {
        store(directories,
                TempArtifactDirectoriesStorage::store,
                "Invalid artifact path provided: {}");
    }

    public void storeStepDirectories(UUID stepId, String... directories) {
        store(directories,
                dir -> TempArtifactDirectoriesStorage.stepStore(stepId, dir),
                "Invalid step artifact path provided: {}");
    }

    private void store(String[] directories, Consumer<String> storage, String logMessage) {
        if (directories == null) {
            return;
        }

        for (String dir : directories) {
            if (!isValidFilePath(dir)) {
                log.info(logMessage, dir);
                continue;
            }
            storage.accept(dir);
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
            throw new ArtifactManagementException(
                "Unknown exception while file path validation", e);
        }
    }
}
