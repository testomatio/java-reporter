package io.testomat.core.artifact.client;

import io.testomat.core.artifact.credential.CredentialsManager;
import io.testomat.core.artifact.credential.CredentialsValidationService;
import io.testomat.core.artifact.credential.S3Credentials;
import io.testomat.core.exception.ArtifactManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class AwsClient {
    private static final Logger log = LoggerFactory.getLogger(AwsClient.class);
    private final CredentialsValidationService validationService;
    private volatile S3Client s3Client;

    public AwsClient() {
        this.validationService = new CredentialsValidationService();
    }

    public AwsClient(CredentialsValidationService validationService) {
        this.validationService = validationService;
    }

    public S3Client getS3Client() {
        S3Client client = s3Client;
        if (client == null) {
            synchronized (this) {
                client = s3Client;
                if (client == null) {
                    log.debug("Lazy initializing S3 Client");
                    s3Client = client = initialize();
                }
            }
        }
        return client;
    }

    private S3Client initialize() {
        log.debug("Initializing S3 Client");
        S3Credentials s3Credentials = CredentialsManager.getCredentials();

        if (!validationService.areCredentialsValid(s3Credentials)) {
            log.error("S3 credentials validation failed during client initialization. The artifacts won't be handled");
        }

        log.debug("Creating S3 client for region: {}, bucket: {}",
                s3Credentials.getRegion(), s3Credentials.getBucket());

        AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(
                s3Credentials.getAccessKeyId(),
                s3Credentials.getSecretAccessKey());

        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(basicCredentials);
        S3Client client = S3Client.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.of(s3Credentials.getRegion()))
                .build();

        log.info("S3 Client initialized successfully for region: {}", s3Credentials.getRegion());
        return client;
    }
}
