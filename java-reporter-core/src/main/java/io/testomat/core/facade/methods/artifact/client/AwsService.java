package io.testomat.core.facade.methods.artifact.client;

import io.testomat.core.facade.methods.artifact.ArtifactLinkData;
import io.testomat.core.facade.methods.artifact.ArtifactLinkDataStorage;
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import io.testomat.core.facade.methods.artifact.util.ArtifactKeyGenerator;
import io.testomat.core.facade.methods.artifact.util.ArtifactUrlGenerator;
import io.testomat.core.exception.ArtifactManagementException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Service for managing S3 artifact uploads with automatic ACL fallback support.
 * Handles file uploads to S3 buckets with intelligent ACL detection and caching.
 */
public class AwsService {
    private static final Logger log = LoggerFactory.getLogger(AwsService.class);
    private static final Map<String, Boolean> bucketAclSupport = new ConcurrentHashMap<>();

    private static final String ACL_PRIVATE = "private";
    private static final String ACL_PUBLIC_READ = "public-read";
    private static final String ERROR_CODE_ACL_NOT_SUPPORTED = "AccessControlListNotSupported";
    private static final String ERROR_CODE_BUCKET_LOCKED = "BucketMustHaveLockedConfiguration";
    private static final String ERROR_MESSAGE_NO_ACL = "does not allow ACLs";

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

    /**
     * Uploads all artifacts for a specific test to S3.
     *
     * @param testName the name of the test
     * @param rid the request identifier
     * @param testId the unique test identifier
     * @throws IllegalArgumentException if any parameter is null
     */
    public void uploadAllArtifactsForTest(String testName, String rid, String testId) {

        List<String> artifactDirectories = TempArtifactDirectoriesStorage.DIRECTORIES.get();
        if (artifactDirectories.isEmpty()) {
            log.debug("Artifact list is empty for test: {}", testName);
            return;
        }

        S3Credentials credentials = CredentialsManager.getCredentials();
        List<String> uploadedArtifactsLinks = processArtifacts(artifactDirectories, testName, rid, credentials);

        storeArtifactLinkData(testName, rid, testId, uploadedArtifactsLinks);

        // Clear artifact directories after processing
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    private List<String> processArtifacts(List<String> artifactDirectories, String testName, String rid, S3Credentials credentials) {
        List<String> uploadedLinks = new ArrayList<>();

        for (String dir : artifactDirectories) {
            String key = keyGenerator.generateKey(dir, rid, testName);
            uploadArtifact(dir, key, credentials);
            uploadedLinks.add(urlGenerator.generateUrl(credentials.getBucket(), key));
        }

        return uploadedLinks;
    }

    private void storeArtifactLinkData(String testName, String rid, String testId, List<String> uploadedLinks) {
        ArtifactLinkData linkData = new ArtifactLinkData(testName, rid, testId, uploadedLinks);
        ArtifactLinkDataStorage.ARTEFACT_LINK_DATA_STORAGE.add(linkData);
    }

    private void uploadArtifact(String dir, String key, S3Credentials credentials) {
        Objects.requireNonNull(dir, "Directory path cannot be null");
        Objects.requireNonNull(key, "S3 key cannot be null");
        Objects.requireNonNull(credentials, "S3 credentials cannot be null");

        Path path = Paths.get(dir);
        byte[] content = readFileContent(path);

        log.debug("Uploading to S3: bucket={}, key={}, size={} bytes",
                credentials.getBucket(), key, content.length);

        uploadWithAclStrategy(path, key, credentials, content);
    }

    private byte[] readFileContent(Path path) {
        try {
            byte[] content = Files.readAllBytes(path);
            log.debug("Successfully read {} bytes from file: {}", content.length, path);
            return content;
        } catch (IOException e) {
            log.error("Failed to read bytes from path: {}", path, e);
            throw new ArtifactManagementException("Failed to read bytes from path: " + path, e);
        }
    }

    private void uploadWithAclStrategy(Path path, String key, S3Credentials credentials, byte[] content) {
        String bucketName = credentials.getBucket();
        Boolean supportsAcl = bucketAclSupport.get(bucketName);

        if (supportsAcl == null) {
            detectAndUpload(path, key, credentials, content, bucketName);
        } else if (supportsAcl) {
            performUploadWithAcl(path, key, credentials, content);
        } else {
            performUploadWithoutAcl(path, key, credentials, content);
        }
    }

    private void detectAndUpload(Path path, String key, S3Credentials credentials, byte[] content, String bucketName) {
        boolean uploadSuccessful = tryUploadWithAcl(path, key, credentials, content);
        if (uploadSuccessful) {
            bucketAclSupport.put(bucketName, true);
        } else {
            bucketAclSupport.put(bucketName, false);
            performUploadWithoutAcl(path, key, credentials, content);
        }
    }

    private boolean tryUploadWithAcl(Path path, String key, S3Credentials credentials, byte[] content) {
        try {
            PutObjectRequest request = buildUploadRequestWithAcl(credentials, key);
            performS3Upload(request, content);
            log.debug("S3 upload completed successfully with ACL for file: {}", path);
            return true;
        } catch (S3Exception e) {
            return handleS3Exception(e, path, credentials, key);
        } catch (Exception e) {
            handleGenericException(e, path, credentials, key);
            return false;
        }
    }

    private void performUploadWithAcl(Path path, String key, S3Credentials credentials, byte[] content) {
        try {
            PutObjectRequest request = buildUploadRequestWithAcl(credentials, key);
            performS3Upload(request, content);
            log.debug("S3 upload completed successfully with ACL for file: {}", path);
        } catch (Exception e) {
            handleUploadException(e, path, credentials, key);
        }
    }

    private PutObjectRequest buildUploadRequestWithAcl(S3Credentials credentials, String key) {
        String acl = credentials.isPresign() ? ACL_PRIVATE : ACL_PUBLIC_READ;
        return PutObjectRequest.builder()
                .bucket(credentials.getBucket())
                .key(key)
                .acl(acl)
                .build();
    }

    private void performUploadWithoutAcl(Path path, String key, S3Credentials credentials, byte[] content) {
        try {
            PutObjectRequest request = buildUploadRequestWithoutAcl(credentials, key);
            performS3Upload(request, content);
            log.info("S3 upload completed successfully for file: {}", path);
        } catch (Exception e) {
            handleUploadException(e, path, credentials, key);
        }
    }

    private PutObjectRequest buildUploadRequestWithoutAcl(S3Credentials credentials, String key) {
        return PutObjectRequest.builder()
                .bucket(credentials.getBucket())
                .key(key)
                .build();
    }

    private void performS3Upload(PutObjectRequest request, byte[] content) {
        awsClient.getS3Client().putObject(request, RequestBody.fromBytes(content));
    }

    private boolean handleS3Exception(S3Exception e, Path path, S3Credentials credentials, String key) {
        if (isAclNotSupportedError(e)) {
            log.info("Bucket '{}' does not support ACLs, will retry without ACL", credentials.getBucket());
            return false;
        } else {
            handleUploadException(e, path, credentials, key);
            return false;
        }
    }

    private void handleGenericException(Exception e, Path path, S3Credentials credentials, String key) {
        log.error("S3 upload failed for file: {} to bucket: {}, key: {}", path, credentials.getBucket(), key, e);
        throw new ArtifactManagementException("S3 upload failed: " + e.getMessage(), e);
    }

    private void handleUploadException(Exception e, Path path, S3Credentials credentials, String key) {
        if (e instanceof S3Exception) {
            S3Exception s3e = (S3Exception) e;
            log.error("S3 upload failed for file: {} to bucket: {}, key: {} - {} (Status: {})",
                    path, credentials.getBucket(), key, s3e.awsErrorDetails().errorMessage(), s3e.statusCode());
            throw new ArtifactManagementException("S3 upload failed: " + s3e.awsErrorDetails().errorMessage(), e);
        } else {
            log.error("S3 upload failed for file: {} to bucket: {}, key: {}", path, credentials.getBucket(), key, e);
            throw new ArtifactManagementException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    private boolean isAclNotSupportedError(S3Exception e) {
        return (e.statusCode() == 400 &&
                (ERROR_CODE_ACL_NOT_SUPPORTED.equals(e.awsErrorDetails().errorCode()) ||
                        ERROR_CODE_BUCKET_LOCKED.equals(e.awsErrorDetails().errorCode()) ||
                        (e.awsErrorDetails().errorMessage() != null &&
                                e.awsErrorDetails().errorMessage().contains(ERROR_MESSAGE_NO_ACL))));
    }
}
