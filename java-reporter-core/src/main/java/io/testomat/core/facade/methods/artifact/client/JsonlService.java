package io.testomat.core.facade.methods.artifact.client;

import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.model.TestFile;
import io.testomat.core.facade.methods.artifact.model.TestItem;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.core.step.StepData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonlService {

    private static final Logger log = LoggerFactory.getLogger(JsonlService.class);

    public void saveTestArtifacts(String testName, String rid, String testId) {
        List<String> artifactDirectories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        Map<UUID, StepData> stepArtifactDirectories =
                TempArtifactDirectoriesStorage.STEP_DATA.getOrDefault(
                Thread.currentThread().getId(),
                Collections.emptyMap()
            );

        if (artifactDirectories.isEmpty() && stepArtifactDirectories.isEmpty()) {
            log.debug("Artifact list is empty for test: {}", testName);
            return;
        }

        List<TestFile> files = new ArrayList<>();

        if (!artifactDirectories.isEmpty()) {
            files = artifactDirectories.stream()
                .map(TestFile::new)
                .collect(Collectors.toList());
        }

        GlobalRunManager.getInstance().addTestItem(new TestItem(rid, testId, files, null));
    }
}
