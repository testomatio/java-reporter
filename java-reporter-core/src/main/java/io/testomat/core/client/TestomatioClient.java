package io.testomat.core.client;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_DISABLE_PROPERTY_NAME;
import static io.testomat.core.constants.ArtifactPropertyNames.JSONL_PATH_PROPERTY_NAME;
import static io.testomat.core.constants.CommonConstants.RESPONSE_UID_KEY;

import io.testomat.core.InfoDisplay;
import io.testomat.core.facade.methods.artifact.ArtifactLinkDataStorage;
import io.testomat.core.facade.methods.artifact.LinkUploadBodyBuilder;
import io.testomat.core.facade.methods.artifact.ReportedTestStorage;
import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.CredentialsValidationService;
import io.testomat.core.client.http.CustomHttpClient;
import io.testomat.core.client.request.NativeRequestBodyBuilder;
import io.testomat.core.client.request.RequestBodyBuilder;
import io.testomat.core.client.urlbuilder.NativeUrlBuilder;
import io.testomat.core.client.urlbuilder.UrlBuilder;
import io.testomat.core.exception.ArtifactManagementException;
import io.testomat.core.exception.FinishReportFailedException;
import io.testomat.core.exception.ReportingFailedException;
import io.testomat.core.exception.RunCreationFailedException;
import io.testomat.core.facade.methods.artifact.model.AddTestsBatchRequest;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.runmanager.GlobalRunManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default HTTP-based implementation of {@link ApiInterface}.
 * Handles test run lifecycle and result reporting with comprehensive error handling
 * and automatic URL/request body construction for Testomat.io API endpoints.
 */
public class TestomatioClient implements ApiInterface {
    private static final Logger log = LoggerFactory.getLogger(TestomatioClient.class);

    private final UrlBuilder urlBuilder = new NativeUrlBuilder();

    private final String apiKey;
    private final CustomHttpClient client;
    private final RequestBodyBuilder requestBodyBuilder;
    private final CredentialsManager credentialsManager = new CredentialsManager();
    private final LinkUploadBodyBuilder linkUploadBodyBuilder = new LinkUploadBodyBuilder();
    private final CredentialsValidationService credentialsValidationService = new CredentialsValidationService();
    private final PropertyProvider provider;

    /**
     * Creates API client with injectable dependencies.
     * Primarily used for testing with mock implementations.
     *
     * @param apiKey             Testomat.io API key for authentication
     * @param client             HTTP client implementation for network requests
     * @param requestBodyBuilder builder for creating JSON request payloads
     */
    public TestomatioClient(String apiKey,
                            CustomHttpClient client,
                            NativeRequestBodyBuilder requestBodyBuilder) {
        this.apiKey = apiKey;
        this.client = client;
        this.requestBodyBuilder = requestBodyBuilder;
        this.provider =
            PropertyProviderFactoryImpl.getPropertyProviderFactory().getPropertyProvider();
    }

    @Override
    public String createRun(String title) throws IOException {
        log.debug("Creating run with title: {}", title);

        String url = urlBuilder.buildCreateRunUrl();
        log.debug("Creating run with request url: {}", url);
        String requestBody = requestBodyBuilder.buildCreateRunBody(title);

        Map<String, Object> responseBody = client.post(url, requestBody, Map.class);
        log.debug(responseBody.toString());

        if (responseBody == null || !responseBody.containsKey(RESPONSE_UID_KEY)) {
            throw new RunCreationFailedException(
                    "Invalid response: missing UID in create test run response");
        }
        if (responseBody.containsKey("artifacts") && !isArtifactDisabled()) {
            Map<String, Object> creds = (Map<String, Object>) responseBody.get("artifacts");
            credentialsManager.populateCredentials(creds);
            credentialsValidationService.areCredentialsValid(CredentialsManager.getCredentials());
        }
        InfoDisplay.logVersionAndPrintUrls(responseBody);
        log.debug("Created test run with UID: {}", responseBody.get(RESPONSE_UID_KEY));
        return responseBody.get(RESPONSE_UID_KEY).toString();
    }

    @Override
    public void reportTest(String uid, TestResult result) {
        try {
            log.debug("Reporting test result for testId: {}", result.getTestId());

            String url = urlBuilder.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildSingleTestReportBody(result);
            log.debug("Request body: {}", requestBody);
            client.post(url, requestBody, null);

        } catch (Exception e) {
            throw new ReportingFailedException("Failed to report test /n" + e.getMessage());
        }
    }

    @Override
    public void reportTests(String uid, List<TestResult> results) {
        try {
            if (results == null || results.isEmpty()) {
                log.debug("No test results to report");
                return;
            }

            log.debug("Reporting batch of {} test results", results.size());

            String url = urlBuilder.buildReportTestUrl(uid);
            String requestBody = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);
            log.debug("Batch request body: {}", requestBody);

            client.post(url, requestBody, null);
        } catch (Exception e) {
            log.error("Failed to report batch test /n{}", e.getMessage());
            throw new ReportingFailedException("Failed to report batch /n" + e.getMessage());
        }
    }

    @Override
    public void sendTestWithArtifacts(String uid) {
        log.debug("Preparing to secondary batch sending with artifacts");
        String url = urlBuilder.buildReportTestUrl(uid);
        try {
            String requestBody = requestBodyBuilder.buildBatchReportBodyWithArtifacts(
                    ReportedTestStorage.getStorage(), apiKey);
            client.post(url, requestBody, null);
            log.debug("Sent requestBody: {}", requestBody);
        } catch (Exception e) {
            log.error("Failed while secondary batch sending with artifacts");
        }
    }

    @Override
    public void writeArtifactsToJsonl(String uid) {
        AddTestsBatchRequest request =
            GlobalRunManager.getInstance().buildBatchRequest();
        if (request == null || request.getTests() == null || request.getTests().isEmpty()) {
            return;
        }
        String filePath = provider.getProperty(JSONL_PATH_PROPERTY_NAME);
        Path path = Path.of(filePath);
        try {
            Files.createDirectories(path.getParent());
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println(toJson(request));
                log.debug("Wrote {} entries to {}", request.getTests().size(), path);
            }
        } catch (IOException e) {
            log.error("Failed to write JSON file {}", path, e);
        }
    }

    private String toJson(Object value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to serialize JSON", e);
            return "{}";
        }
    }

    @Override
    public void finishTestRun(String uid, float duration) {
        try {
            log.debug("Finishing test run with uid: {}", uid);

            String url = urlBuilder.buildFinishTestRunUrl(uid);
            String requestBody = requestBodyBuilder.buildFinishRunBody(duration);

            client.put(url, requestBody, null);
        } catch (Exception e) {
            log.error("Failed to finish test run with uid: {}", uid);
            throw new FinishReportFailedException("Failed to finish test run " + e.getMessage());
        }
    }

    public void uploadLinksToTestomatio(String uid) {

        String requestBody = linkUploadBodyBuilder.buildLinkUploadRequestBody(
                ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE, apiKey);

        String url = urlBuilder.buildReportTestUrl(uid);
        log.debug("-> REQUEST BODY: {}", requestBody);

        try {
            client.post(url, requestBody, null);
        } catch (IOException e) {
            throw new ArtifactManagementException("Failed to upload artifact links to Testomatio", e);
        }
    }

    private boolean isArtifactDisabled() {
        try {
            return Boolean.parseBoolean(provider.getProperty(ARTIFACT_DISABLE_PROPERTY_NAME));
        } catch (Exception e) {
            return false;
        }
    }
}
