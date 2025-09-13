package io.testomat.core.artifact.client;

import io.testomat.core.artifact.ArtifactKeyGenerator;
import io.testomat.core.artifact.TemporalArtifactStorage;
import io.testomat.core.artifact.credential.CredentialsManager;
import io.testomat.core.artifact.credential.CredentialsValidationService;
import io.testomat.core.artifact.credential.S3Credentials;
import io.testomat.core.exception.ArtifactManagementException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsService {
    private static final Logger log = LoggerFactory.getLogger(AwsService.class);
    private final CredentialsValidationService validationService;
    private final ArtifactKeyGenerator artifactKeyGenerator;

    public AwsService() {
        this.artifactKeyGenerator = new ArtifactKeyGenerator();
        this.validationService = new CredentialsValidationService();
        log.debug("AWS Async Client initialized");
    }

    private volatile S3AsyncClient s3AsyncClient;

    public void uploadAllArtifactsForTest(String testName, String rid) {
        if (TemporalArtifactStorage.DIRECTORIES.get().isEmpty()) {
            log.debug("Artifact list is empty for test: {}", testName);
            CompletableFuture.completedFuture(null);
            return;
        }

        S3Credentials credentials = CredentialsManager.getCredentials();

        List<CompletableFuture<Void>> uploadFutures = TemporalArtifactStorage.DIRECTORIES.get().stream()
                .map(dir -> {
                    String key = artifactKeyGenerator.generateKey(dir, rid, testName);
                    return uploadArtifact(dir, key, credentials);
                })
                .collect(Collectors.toList());

        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> uploadArtifact(String dir, String key, S3Credentials credentials) {
        byte[] content;
        Path path = Paths.get(dir);
        try {
            content = Files.readAllBytes(path);
            log.debug("Successfully read {} bytes from file: {}", content.length, path);
        } catch (IOException e) {
            log.error("Failed to read bytes from path: {}", path, e);
            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new ArtifactManagementException("Failed to read bytes from path: " + path));
            return failedFuture;
        }
        
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(credentials.getBucket())
                .key(key)
                .build();

        log.debug("Uploading to S3: bucket={}, key={}, size={} bytes",
                credentials.getBucket(), key, content.length);

        return getOrCreateClient().putObject(request, AsyncRequestBody.fromBytes(content))
                .thenAccept(response -> {
                    log.info("S3 upload completed successfully for file: {} (ETag: {})", path, response.eTag());
                })
                .exceptionally(throwable -> {
                    log.error("S3 upload failed for file: {} to bucket: {}, key: {}", path, credentials.getBucket(), key, throwable);
                    throw new ArtifactManagementException("S3 upload failed: " + throwable.getMessage(), throwable);
                });
    }

    private S3AsyncClient initialize() {
        log.debug("Initializing S3 Client");
        S3Credentials s3Credentials = CredentialsManager.getCredentials();

        if (!validationService.areCredentialsValid(s3Credentials)) {
            log.error("S3 credentials validation failed during client initialization");
            throw new ArtifactManagementException("Invalid S3 credentials provided");
        }

        log.debug("Creating S3 client for region: {}, bucket: {}",
                s3Credentials.getRegion(), s3Credentials.getBucket());

        AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(
                s3Credentials.getAccessKeyId(),
                s3Credentials.getSecretAccessKey());

        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(basicCredentials);
        S3AsyncClient client = S3AsyncClient.builder()
                .credentialsProvider(staticCredentialsProvider)
                .multipartEnabled(true)
                .region(Region.of(s3Credentials.getRegion()))
                .build();

        log.info("S3 Async Client initialized successfully for region: {}", s3Credentials.getRegion());
        return client;
    }

    private S3AsyncClient getOrCreateClient() {
        S3AsyncClient client = s3AsyncClient;
        if (client == null) {
            synchronized (this) {
                client = s3AsyncClient;
                if (client == null) {
                    log.debug("Lazy initializing S3 Async Client");
                    s3AsyncClient = client = initialize();
                }
            }
        }
        return client;
    }
}
