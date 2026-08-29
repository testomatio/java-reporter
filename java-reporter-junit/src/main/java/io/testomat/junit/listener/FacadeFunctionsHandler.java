package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.JSONL_EXPORT_PROPERTY_NAME;

import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.logmethod.LogStorage;
import io.testomat.core.facade.methods.meta.MetaStorage;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.junit.extractor.JunitMetaDataExtractor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FacadeFunctionsHandler {
    private final PropertyProvider provider;
    private final AwsService awsService;
    private final JsonlService jsonlService;
    private boolean artifactDisabled = false;
    private boolean jsonlDisabled = false;

    public FacadeFunctionsHandler() {
        this.awsService = new AwsService();
        this.jsonlService = new JsonlService();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.artifactDisabled = defineArtifactsDisabled();
        this.jsonlDisabled = defineJsonlExportEnabled();
    }

    public FacadeFunctionsHandler(boolean artifactDisabled,
                                  PropertyProvider provider,
                                  AwsService awsService,
                                  JsonlService jsonlService) {
        this.awsService = awsService;
        this.jsonlService = jsonlService;
        this.artifactDisabled = artifactDisabled;
        this.jsonlDisabled = defineJsonlExportEnabled();
        this.provider = provider;
    }

    public void handleFacadeFunctions(ExtensionContext context) {
        handleLogsAfterEach(context);
        handleMetaAfterEach(context);
        handleLabels(context);
        handleJsonlAfterEach(context);
        handleArtifactsAfterEach(context);
    }

    private void handleMetaAfterEach(ExtensionContext context) {
        String rid = context.getUniqueId();
        Map<String, String> metaData = MetaStorage.getTempMetaStorage();

        if (!metaData.isEmpty()) {
            MetaStorage.getLinkedMetaStorage().put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.clearTempMetaStorage();
        }
    }

    private void handleLogsAfterEach(ExtensionContext context) {
        String rid = context.getUniqueId();
        List<String> storedLogs = LogStorage.TEMP_LOG_STORAGE.get();
        if (!storedLogs.isEmpty()) {
            String[] logs = new String[storedLogs.size()];
            logs = storedLogs.toArray(logs);
            LogStorage.LINKED_LOG_STORAGE.put(rid, logs);
        }
    }

    private void handleArtifactsAfterEach(ExtensionContext context) {
        if (!artifactDisabled) {
            awsService.uploadAllArtifactsForTest(context.getDisplayName(), context.getUniqueId(),
                    JunitMetaDataExtractor.extractTestId(context.getTestMethod().get()));
        }
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    private void handleJsonlAfterEach(ExtensionContext context) {
        if (!jsonlDisabled) {
            jsonlService.saveTestArtifacts(context.getDisplayName(), context.getUniqueId(),
                    JunitMetaDataExtractor.extractTestId(context.getTestMethod().get()));
        }
    }

    private void handleLabels(ExtensionContext context) {
        String rid = context.getUniqueId();
        List<Map<String, String>> storedLabels = LabelStorage.getTempLabelStorage();
        if (!storedLabels.isEmpty()) {
            LabelStorage.getLinkedLabelStorage().put(rid, storedLabels);
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
