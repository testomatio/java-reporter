package io.testomat.core.artifact.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 client wrapper with built-in configuration management.
 * Provides singleton S3Client instances with custom endpoint and credential support.
 */
public class AwsClient {
    private static final Logger log = LoggerFactory.getLogger(AwsClient.class);
    private volatile S3Client s3Client;
    private final S3ClientFactory clientFactory;

    public AwsClient() {
        this.clientFactory = new S3ClientFactory();
    }

    /**
     * Test Constructor
     */
    public AwsClient(S3ClientFactory s3ClientFactory) {
        this.clientFactory = s3ClientFactory;
    }

    /**
     * Returns a configured S3Client instance using lazy initialization.
     *
     * @return configured S3Client instance
     */
    public S3Client getS3Client() {
        if (s3Client == null) {
            s3Client = clientFactory.createS3Client();
            return s3Client;
        }
        return s3Client;
    }
}
