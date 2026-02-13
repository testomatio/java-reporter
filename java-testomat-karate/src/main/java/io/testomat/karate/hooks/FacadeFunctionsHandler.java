package io.testomat.karate.hooks;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.List;
import java.util.Map;

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
        handleArtifactsAfterEach(sr);
    }

    private void handleMetaAfterEach(String rid) {
        Map<String, String> metaData = MetaStorage.TEMP_META_STORAGE.get();

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
        }
    }

    private void handleArtifactsAfterEach(ScenarioRuntime sr) {
        awsService.uploadAllArtifactsForTest(dataExtractor.extractTitle(sr),
            dataExtractor.getRid(sr),
                dataExtractor.extractTestId(sr));
    }

    private void handleLabels(String rid) {
        List<Map<String, String>> storedLabels = LabelStorage.TEMP_LABEL_STORAGE.get();
        if (!storedLabels.isEmpty()) {
            LabelStorage.LINKED_LABEL_STORAGE.put(rid, storedLabels);
        }
    }

    private void handleLogFunction(String rid) {
        List<String> storedLogs = LogStorage.TEMP_LOG_STORAGE.get();
        if (!storedLogs.isEmpty()) {
            String[] logs = new String[storedLogs.size()];
            logs = storedLogs.toArray(logs);
            LogStorage.LINKED_LOG_STORAGE.put(rid, logs);
        }
    }
}
