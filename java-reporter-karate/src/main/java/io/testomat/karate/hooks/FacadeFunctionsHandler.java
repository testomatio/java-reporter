package io.testomat.karate.hooks;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.JSONL_EXPORT_PROPERTY_NAME;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FacadeFunctionsHandler {

    private final PropertyProvider provider;
    private final AwsService awsService;
    private final JsonlService jsonlService;
    private final TestDataExtractor dataExtractor;

    public FacadeFunctionsHandler(
            PropertyProvider provider,
            AwsService awsService,
            JsonlService jsonlService,
            TestDataExtractor dataExtractor) {
        this.provider = provider;
        this.dataExtractor = dataExtractor;
        this.awsService = awsService;
        this.jsonlService = jsonlService;
    }

    public FacadeFunctionsHandler() {
        this.provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.dataExtractor = new TestDataExtractor();
        this.awsService = new AwsService();
        this.jsonlService = new JsonlService();
    }

    public void handleFacadeFunctions(ScenarioRuntime sr) {
        String rid = dataExtractor.getRid(sr);
        handleMetaAfterEach(rid);
        handleLogFunction(rid);
        handleLabels(rid);
        handleJsonlAfterEach(sr, rid);
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
        if (!defineArtifactsDisabled()) {
            awsService.uploadAllArtifactsForTest(
                    dataExtractor.extractTitle(sr),
                    rid,
                    dataExtractor.extractTestId(sr)
            );
        }
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    private void handleJsonlAfterEach(ScenarioRuntime sr, String rid) {
        if (!defineJsonlExportEnabled()) {
            jsonlService.saveTestArtifacts(
                    dataExtractor.extractTitle(sr),
                    rid,
                    dataExtractor.extractTestId(sr)
            );
        }
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
