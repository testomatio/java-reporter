package io.testomat.core.artifact.client;

import io.testomat.core.artifact.credential.CredentialsValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class AwsClient {
    private static final Logger log = LoggerFactory.getLogger(AwsClient.class);
    private final CredentialsValidationService validationService;
    private volatile S3Client s3Client;
    private S3ClientFactory clientFactory;

    public AwsClient() {
        this.validationService = new CredentialsValidationService();
        this.clientFactory = new S3ClientFactory();
    }

    public AwsClient(CredentialsValidationService validationService, S3ClientFactory s3ClientFactory) {
        this.validationService = validationService;
        this.clientFactory = s3ClientFactory;
    }

    public S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = clientFactory.createS3Client();
            return s3Client;
        }
        return s3Client;
    }
}
