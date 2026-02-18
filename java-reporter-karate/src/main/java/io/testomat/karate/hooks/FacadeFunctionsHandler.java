package io.testomat.karate.hooks;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FacadeFunctionsHandler {

    private final AwsService awsService;
    private final TestDataExtractor dataExtractor;

    public FacadeFunctionsHandler(AwsService awsService, TestDataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
        this.awsService = awsService;
    }

    public FacadeFunctionsHandler() {
        this.dataExtractor = new TestDataExtractor();
        this.awsService = new AwsService();
    }

    public void handleFacadeFunctions(ScenarioRuntime sr) {
        String rid = dataExtractor.getRid(sr);
        handleMetaAfterEach(rid);
        handleLogFunction(rid);
        handleLabels(rid);
        handleArtifactsAfterEach(sr, rid);
    }

    private void handleMetaAfterEach(String rid) {
        Map<String, String> metaData =
                Optional.ofNullable(MetaStorage.TEMP_META_STORAGE.get())
                .orElse(Map.of());

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
        }
    }

    private void handleArtifactsAfterEach(ScenarioRuntime sr, String rid) {
        awsService.uploadAllArtifactsForTest(
                dataExtractor.extractTitle(sr),
                rid,
                dataExtractor.extractTestId(sr)
        );
    }

    private void handleLabels(String rid) {
        List<Map<String, String>> storedLabels =
                Optional.ofNullable(LabelStorage.TEMP_LABEL_STORAGE.get())
                .orElse(List.of());

        if (!storedLabels.isEmpty()) {
            LabelStorage.LINKED_LABEL_STORAGE.put(rid, List.copyOf(storedLabels));
        }
    }

    private void handleLogFunction(String rid) {
        List<String> storedLogs =
                Optional.ofNullable(LogStorage.TEMP_LOG_STORAGE.get())
                .orElse(List.of());

        if (!storedLogs.isEmpty()) {
            LogStorage.LINKED_LOG_STORAGE.put(rid, storedLogs.toArray(String[]::new));
        }
    }
}
