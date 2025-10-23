package io.testomat.core.facade.methods.artifact;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe storage for artifact link data collected during test execution.
 * Maintains a collection of artifact links that will be associated with test results.
 */
public class ArtifactLinkDataStorage {
    public static final List<ArtifactLinkData> ARTEFACT_LINK_DATA_STORAGE =
            new CopyOnWriteArrayList<>();
}
