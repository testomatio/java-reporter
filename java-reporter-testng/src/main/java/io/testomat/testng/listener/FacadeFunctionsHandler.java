package io.testomat.testng.listener;

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
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import java.util.List;
import java.util.Map;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;

public class FacadeFunctionsHandler {
    private final TestNgParameterExtractor testNgParameterExtractor;
    private final TestNgMetaDataExtractor metaDataExtractor;
    private final PropertyProvider provider;
    private final AwsService awsService;
    private final JsonlService jsonlService;

    public FacadeFunctionsHandler() {
        this.testNgParameterExtractor = new TestNgParameterExtractor();
        this.metaDataExtractor = new TestNgMetaDataExtractor();
        this.awsService = new AwsService();
        this.jsonlService = new JsonlService();
        this.provider =
                PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    public FacadeFunctionsHandler(TestNgParameterExtractor testNgParameterExtractor,
                                  TestNgMetaDataExtractor metaDataExtractor,
                                  PropertyProvider provider,
                                  AwsService awsService,
                                  JsonlService jsonlService) {
        this.testNgParameterExtractor = testNgParameterExtractor;
        this.metaDataExtractor = metaDataExtractor;
        this.awsService = awsService;
        this.jsonlService = jsonlService;
        this.provider = provider;
    }

    public void handleFacadeFunctions(IInvokedMethod method, ITestResult testResult) {
        handleMetaAfterInvocation(testResult);
        handleLogsAfterInvocation(testResult);
        handleJsonlAfterInvocation(method, testResult);
        handleArtifactsAfterInvocation(method, testResult);
        handleLabels(testResult);
    }

    private void handleMetaAfterInvocation(ITestResult testResult) {
        String rid = testNgParameterExtractor.generateRid(testResult);
        Map<String, String> metaData = MetaStorage.getTempMetaStorage();

        if (!metaData.isEmpty()) {
            MetaStorage.getLinkedMetaStorage().put(rid, new java.util.HashMap<>(metaData));
            MetaStorage.clearTempMetaStorage();
        }
    }

    private void handleArtifactsAfterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!defineArtifactsDisabled()) {
            awsService.uploadAllArtifactsForTest(testResult.getName(),
                    testNgParameterExtractor.generateRid(testResult),
                    metaDataExtractor.getTestId(
                            method.getTestMethod().getConstructorOrMethod().getMethod())
            );
        }
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    private void handleJsonlAfterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!defineJsonlExportEnabled()) {
            jsonlService.saveTestArtifacts(testResult.getName(),
                    testNgParameterExtractor.generateRid(testResult),
                    metaDataExtractor.getTestId(
                    method.getTestMethod().getConstructorOrMethod().getMethod()));
        }
    }

    private void handleLogsAfterInvocation(ITestResult testResult) {
        String rid = testNgParameterExtractor.generateRid(testResult);
        List<String> storedLogs = LogStorage.TEMP_LOG_STORAGE.get();
        if (!storedLogs.isEmpty()) {
            String[] logs = new String[storedLogs.size()];
            logs = storedLogs.toArray(logs);
            LogStorage.LINKED_LOG_STORAGE.put(rid, logs);
        }
    }

    private void handleLabels(ITestResult testResult) {
        String rid = testNgParameterExtractor.generateRid(testResult);
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
