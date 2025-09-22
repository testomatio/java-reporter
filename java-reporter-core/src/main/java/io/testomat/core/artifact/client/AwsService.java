package io.testomat.core.artifact.client;

import io.testomat.core.ArtifactLinkData;
import io.testomat.core.ArtifactLinkDataStorage;
import io.testomat.core.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.artifact.credential.CredentialsManager;
import io.testomat.core.artifact.credential.S3Credentials;
import io.testomat.core.artifact.util.ArtifactKeyGenerator;
import io.testomat.core.artifact.util.ArtifactUrlGenerator;
import io.testomat.core.exception.ArtifactManagementException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class AwsService {
    private static final Logger log = LoggerFactory.getLogger(AwsService.class);
    private static final Map<String, Boolean> bucketAclSupport = new ConcurrentHashMap<>();

    private final ArtifactKeyGenerator keyGenerator;
    private final ArtifactUrlGenerator urlGenerator;
    private final AwsClient awsClient;

    public AwsService() {
        this.keyGenerator = new ArtifactKeyGenerator();
        this.awsClient = new AwsClient();
        this.urlGenerator = new ArtifactUrlGenerator();
        log.debug("AWS Service initialized");
    }

    public AwsService(ArtifactKeyGenerator keyGenerator, AwsClient awsClient,
                      ArtifactUrlGenerator urlGenerator) {
        this.keyGenerator = keyGenerator;
        this.awsClient = awsClient;
        this.urlGenerator = urlGenerator;
    }

    public void uploadAllArtifactsForTest(String testName, String rid, String testId) {
        if (TempArtifactDirectoriesStorage.DIRECTORIES.get().isEmpty()) {
            log.debug("Artifact list is empty for test: {}", testName);
            return;
        }

        S3Credentials credentials = CredentialsManager.getCredentials();
        List<String> uploadedArtifactsLinks = new ArrayList<>();

        for (String dir : TempArtifactDirectoriesStorage.DIRECTORIES.get()) {
            String key = keyGenerator.generateKey(dir, rid, testName);
            uploadArtifact(dir, key, credentials);
            uploadedArtifactsLinks.add(urlGenerator.generateUrl(credentials.getBucket(), key));
        }

        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(new ArtifactLinkData(testName, rid, testId, uploadedArtifactsLinks));
    }

    private void uploadArtifact(String dir, String key, S3Credentials credentials) {
        byte[] content;
        Path path = Paths.get(dir);
        try {
            content = Files.readAllBytes(path);
            log.debug("Successfully read {} bytes from file: {}", content.length, path);
        } catch (IOException e) {
            log.error("Failed to read bytes from path: {}", path, e);
            throw new ArtifactManagementException("Failed to read bytes from path: " + path);
        }

        log.debug("Uploading to S3: bucket={}, key={}, size={} bytes",
                credentials.getBucket(), key, content.length);

        String bucketName = credentials.getBucket();
        Boolean supportsAcl = bucketAclSupport.get(bucketName);

        if (supportsAcl == null) {
            boolean uploadSuccessful = tryUploadWithAcl(path, key, credentials, content);
            if (!uploadSuccessful) {
                bucketAclSupport.put(bucketName, false);
                tryUploadWithoutAcl(path, key, credentials, content);
            } else {
                bucketAclSupport.put(bucketName, true);
            }
        } else if (supportsAcl) {
            tryUploadWithAcl(path, key, credentials, content);
        } else {
            tryUploadWithoutAcl(path, key, credentials, content);
        }
    }

    private boolean tryUploadWithAcl(Path path, String key, S3Credentials credentials, byte[] content) {
        try {
            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                    .bucket(credentials.getBucket())
                    .key(key);

            if (credentials.isPresign()) {
                builder.acl("private");
            } else {
                builder.acl("public-read");
            }
            PutObjectRequest request = builder.build();

            awsClient.getS3Client().putObject(request, RequestBody.fromBytes(content));
            log.debug("S3 upload completed successfully with ACL for file: {}", path);
            return true;
        } catch (S3Exception e) {
            if (isAclNotSupportedError(e)) {
                log.info("Bucket '{}' does not support ACLs, will retry without ACL", credentials.getBucket());
                return false;
            } else {
                log.error("S3 upload failed for file: {} to bucket: {}, key: {} - {} (Status: {})",
                        path, credentials.getBucket(), key, e.awsErrorDetails().errorMessage(), e.statusCode());
                throw new ArtifactManagementException("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);
            }
        } catch (Exception e) {
            log.error("S3 upload failed for file: {} to bucket: {}, key: {}", path, credentials.getBucket(), key, e);
            throw new ArtifactManagementException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    private void tryUploadWithoutAcl(Path path, String key, S3Credentials credentials, byte[] content) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(credentials.getBucket())
                    .key(key)
                    .build();

            awsClient.getS3Client().putObject(request, RequestBody.fromBytes(content));
            log.info("S3 upload completed successfully for file: {}", path);
        } catch (S3Exception e) {
            log.error("S3 upload failed for file: {} to bucket: {}, key: {} - {} (Status: {})",
                    path, credentials.getBucket(), key, e.awsErrorDetails().errorMessage(), e.statusCode());
            throw new ArtifactManagementException("S3 upload failed: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("S3 upload failed for file: {} to bucket: {}, key: {}", path, credentials.getBucket(), key, e);
            throw new ArtifactManagementException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    private boolean isAclNotSupportedError(S3Exception e) {
        return (e.statusCode() == 400 &&
                ("AccessControlListNotSupported".equals(e.awsErrorDetails().errorCode()) ||
                        "BucketMustHaveLockedConfiguration".equals(e.awsErrorDetails().errorCode()) ||
                        (e.awsErrorDetails().errorMessage() != null &&
                                e.awsErrorDetails().errorMessage().contains("does not allow ACLs"))));
    }
}
