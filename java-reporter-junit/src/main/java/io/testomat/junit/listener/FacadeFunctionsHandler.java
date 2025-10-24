package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;

import io.testomat.core.facade.methods.artifact.client.AwsService;
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
    private boolean artifactDisabled = false;

    public FacadeFunctionsHandler() {
        this.awsService = new AwsService();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
        this.artifactDisabled = defineArtifactsDisabled();
    }

    public FacadeFunctionsHandler(boolean artifactDisabled,
                                  PropertyProvider provider,
                                  AwsService awsService) {
        this.awsService = awsService;
        this.artifactDisabled = artifactDisabled;
        this.provider = provider;
    }

    public void handleFacadeFunctions(ExtensionContext context) {
        handleLogsAfterEach(context);
        handleMetaAfterEach(context);
        handleArtifactsAfterEach(context);
    }

    private void handleMetaAfterEach(ExtensionContext context) {
        String rid = context.getUniqueId();
        Map<String, String> metaData = MetaStorage.TEMP_META_STORAGE.get();

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
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
}
