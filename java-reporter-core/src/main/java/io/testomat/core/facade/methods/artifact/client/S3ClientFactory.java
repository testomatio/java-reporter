package io.testomat.core.facade.methods.artifact.client;

import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Factory for creating configured S3Client instances with custom endpoint support.
 * Handles AWS credentials, regions, and S3-compatible storage configurations.
 */
public class S3ClientFactory {

    /**
     * Creates a configured S3Client based on current credentials and settings.
     *
     * @return configured S3Client instance
     * @throws IllegalArgumentException if credentials are invalid or missing
     */
    public S3Client createS3Client() {
        S3Credentials s3Credentials = CredentialsManager.getCredentials();

        S3ClientBuilder builder = S3Client.builder();

        AwsCredentialsProvider credentialsProvider;
        if (s3Credentials.getAccessKeyId() != null && s3Credentials.getSecretAccessKey() != null) {
            if (s3Credentials.getAccessKeyId().trim().isEmpty() || s3Credentials.getSecretAccessKey().trim().isEmpty()) {
                throw new IllegalArgumentException("Access key and secret access key cannot be empty");
            }
            AwsBasicCredentials credentials = AwsBasicCredentials.create(s3Credentials.getAccessKeyId().trim(), s3Credentials.getSecretAccessKey().trim());
            credentialsProvider = StaticCredentialsProvider.create(credentials);
        } else {
            throw new IllegalArgumentException("S3 credentials (access key and secret access key) must be configured");
        }
        builder.credentialsProvider(credentialsProvider);

        if (s3Credentials.getRegion() != null && !s3Credentials.getRegion().trim().isEmpty()) {
            try {
                builder.region(Region.of(s3Credentials.getRegion().trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid region: " + s3Credentials.getRegion(), e);
            }
        } else {
            builder.region(Region.US_EAST_1);
        }

        if (s3Credentials.getCustomEndpoint() != null && !s3Credentials.getCustomEndpoint().trim().isEmpty()) {
            try {
                builder.endpointOverride(URI.create(s3Credentials.getCustomEndpoint().trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid endpoint URL: " + s3Credentials.getCustomEndpoint(), e);
            }

            S3Configuration s3Config = S3Configuration.builder()
                    .pathStyleAccessEnabled(s3Credentials.isForcePath())
                    .build();
            builder.serviceConfiguration(s3Config);
        } else {
            if (s3Credentials.isForcePath()) {
                S3Configuration s3Config = S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build();
                builder.serviceConfiguration(s3Config);
            }
        }

        return builder.build();
    }
}