package io.testomat.core.artifact.credential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class CredentialsValidationService {
    private static final Logger log = LoggerFactory.getLogger(CredentialsValidationService.class);

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

        try (S3Client client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(creds.getAccessKeyId(), creds.getSecretAccessKey())))
                .region(Region.of(creds.getRegion()))
                .build()) {

            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(creds.getBucket())
                    .build();

            client.headBucket(headBucketRequest);
            log.info("S3 credentials validation successful for bucket: {} in region: {}",
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
