package io.testomat.core.artifact;

import io.testomat.core.exception.ArtifactManagementException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactManager {
    private static final Logger log = LoggerFactory.getLogger(ArtifactManager.class);
    private final DelayedArtifactHandler delayedArtifactHandler;
    private final ArtifactHandler artifactHandler;

    public ArtifactManager() {
        this.artifactHandler = new ArtifactHandler();
        this.delayedArtifactHandler = new DelayedArtifactHandler();
    }

    public ArtifactManager(DelayedArtifactHandler delayedArtifactHandler, ArtifactHandler artifactHandler) {
        this.delayedArtifactHandler = delayedArtifactHandler;
        this.artifactHandler = artifactHandler;
    }

    public void manageArtifact(Method method, String... directories) {
        if (method == null) {
            throw new ArtifactManagementException("Received null method into ArtifactManager");
        }
        for (String dir : directories) {
            try {
                if (isValidFilePath(dir)) {
                    if (shouldArtifactBeDelayed(dir)) {
                        delayedArtifactHandler.handleArtifact(method, dir);
                    } else {
                        artifactHandler.handleArtifact(method, dir);
                    }
                }
            } catch (InvalidPathException | SecurityException e) {
                log.warn("Failed to validate path: {}", dir, e);
            }
        }
    }

    private boolean shouldArtifactBeDelayed(String dir) {
        for (String format : DelayedArtifactFormats.FORMATS) {
            if (dir.endsWith(format)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isValidFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            return Files.exists(path) && Files.isRegularFile(path);
        } catch (InvalidPathException e) {
            log.warn("Provided filepath is not valid: {}", filePath);
            return false;
        }
    }
}
