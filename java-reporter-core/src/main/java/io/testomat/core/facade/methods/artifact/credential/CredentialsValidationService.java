package io.testomat.core.facade.methods.artifact.credential;

import io.testomat.core.facade.methods.artifact.client.AwsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Service for validating S3 credentials by performing actual S3 operations.
 * Uses HeadBucket operation to verify bucket access and credential validity.
 */
public class CredentialsValidationService {
    private static final Logger log = LoggerFactory.getLogger(CredentialsValidationService.class);
    private final AwsClient awsClient;

    public CredentialsValidationService() {
        this.awsClient = new AwsClient();
    }

    public CredentialsValidationService(AwsClient awsClient) {
        this.awsClient = awsClient;
    }

    /**
     * Validates S3 credentials by attempting a HeadBucket operation.
     *
     * @param creds the S3 credentials to validate
     * @return true if credentials are valid and bucket is accessible, false otherwise
     */
    public boolean areCredentialsValid(S3Credentials creds) {
        if (creds == null) {
            log.error("Cannot validate null S3 credentials");
            return false;
        }

        if (creds.getAccessKeyId() == null || creds.getSecretAccessKey() == null ||
                creds.getRegion() == null || creds.getBucket() == null) {
            log.error("S3 credentials validation failed: missing required fields - " +
                            "accessKey: {}, secretKey: {}, region: {}, bucket: {}",
                    creds.getAccessKeyId() != null, creds.getSecretAccessKey() != null,
                    creds.getRegion() != null, creds.getBucket() != null);
            return false;
        }

        log.debug("Validating S3 credentials for bucket: {} in region: {}",
                creds.getBucket(), creds.getRegion());

        try {
            S3Client client = awsClient.getS3Client();
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(creds.getBucket())
                    .build();

            client.headBucket(headBucketRequest);
            log.debug("S3 credentials validation successful for bucket: {} in region: {}",
                    creds.getBucket(), creds.getRegion());
            return true;
        } catch (S3Exception e) {
            log.error("S3 credentials validation failed for bucket: {} - {} (Status: {})",
                    creds.getBucket(), e.awsErrorDetails().errorMessage(), e.statusCode());
            return false;
        } catch (Exception e) {
            log.error("S3 connection error during validation for bucket: {} - {}",
                    creds.getBucket(), e.getMessage(), e);
            return false;
        }
    }
}
