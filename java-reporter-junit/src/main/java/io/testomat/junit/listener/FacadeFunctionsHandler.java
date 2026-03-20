package io.testomat.junit.listener;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;

import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.facade.methods.linkjira.JiraLinkStorage;
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
        String rid = context.getUniqueId();
        handleLogsAfterEach(rid);
        handleMetaAfterEach(rid);
        handleLabels(rid);
        handleLinkJira(rid);
        handleArtifactsAfterEach(context);
    }

    private void handleMetaAfterEach(String rid) {
        Map<String, String> metaData = MetaStorage.TEMP_META_STORAGE.get();

        if (!metaData.isEmpty()) {
            MetaStorage.LINKED_META_STORAGE.put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.TEMP_META_STORAGE.remove();
        }
    }

    private void handleLogsAfterEach(String rid) {
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

    private void handleLabels(String rid) {
        List<Map<String, String>> storedLabels = LabelStorage.TEMP_LABEL_STORAGE.get();
        if (!storedLabels.isEmpty()) {
            LabelStorage.LINKED_LABEL_STORAGE.put(rid, storedLabels);
        }
    }

    private void handleLinkJira(String rid) {
        List<String> storedLinks = JiraLinkStorage.TEMP_JIRA_LINK_STORAGE.get();
        if (!storedLinks.isEmpty()) {
            JiraLinkStorage.LINKED_JIRA_LINK_STORAGE.put(rid, storedLinks);
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
