package io.testomat.core.facade.methods.artifact.client;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Factory for creating configured S3Client instances with custom endpoint support.
 * Handles AWS credentials, regions, and S3-compatible storage configurations.
 */
public class S3ClientFactory {

    public S3Client createS3Client() {
        S3ClientBuilder builder = S3Client.builder();
        S3ClientConfiguration.configure(builder);

        return builder.build();
    }
}