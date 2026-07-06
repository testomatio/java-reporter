package io.testomat.cucumber.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.JSONL_EXPORT_PROPERTY_NAME;

import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.cucumber.extractor.TestDataExtractor;
import java.util.List;
import java.util.Map;

public class FacadeFunctionsHandler {
    private final AwsService awsService;
    private final JsonlService jsonlService;
    private final TestDataExtractor dataExtractor;
    private final PropertyProvider provider;

    public FacadeFunctionsHandler(AwsService awsService, JsonlService jsonlService,
                                  TestDataExtractor dataExtractor, PropertyProvider provider) {
        this.dataExtractor = dataExtractor;
        this.awsService = awsService;
        this.jsonlService = jsonlService;
        this.provider = provider;
    }

    public FacadeFunctionsHandler() {
        this.dataExtractor = new TestDataExtractor();
        this.awsService = new AwsService();
        this.jsonlService = new JsonlService();
        this.provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    public void handleFacadeFunctions(TestCaseFinished testCaseFinished) {
        String rid = testCaseFinished.getTestCase().getId().toString();
        handleMetaAfterEach(rid);
        handleLogFunction(rid);
        handleLabels(rid);
        handleArtifactsAfterEach(testCaseFinished);
    }

    private void handleMetaAfterEach(String rid) {
        Map<String, String> metaData = MetaStorage.TEMP_META_STORAGE.get();

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
        }
    }

    private void handleArtifactsAfterEach(TestCaseFinished testCaseFinished) {
        if (!defineArtifactsDisabled()) {
            awsService.uploadAllArtifactsForTest(dataExtractor.extractTitle(testCaseFinished),
                    testCaseFinished.getTestCase().getId().toString(),
                    dataExtractor.extractTestId(testCaseFinished));
        }
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    private void handleJsonlAfterEach(TestCaseFinished testCaseFinished) {
        if (!defineJsonlExportEnabled()) {
            jsonlService.saveTestArtifacts(dataExtractor.extractTitle(testCaseFinished),
                    testCaseFinished.getTestCase().getId().toString(),
                    dataExtractor.extractTestId(testCaseFinished));
        }
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

    private boolean defineArtifactsDisabled() {
        boolean result;
        String property;
        try {
            property = provider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME);
            result = property != null
                && !property.trim().isEmpty()
                && !property.equalsIgnoreCase("0");

        } catch (Exception e) {
            return false;
        }
        return result;
    }

    private boolean defineJsonlExportEnabled() {
        try {
            return Boolean.parseBoolean(
                provider.getProperty(JSONL_EXPORT_PROPERTY_NAME)
            );
        } catch (Exception e) {
            return false;
        }
    }
}
