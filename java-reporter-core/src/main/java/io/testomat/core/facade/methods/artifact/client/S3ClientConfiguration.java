package io.testomat.core.facade.methods.artifact.client;

import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3BaseClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Utility class for configuring AWS S3 client builders.
 */
public final class S3ClientConfiguration {

    private S3ClientConfiguration() {
    }

    /**
     * Applies common S3 configuration to the specified client builder.
     */
    public static <T extends S3BaseClientBuilder<?, ?>> void configure(T builder) {
        S3Credentials credentials = CredentialsManager.getCredentials();

        String accessKey =
            require(credentials.getAccessKeyId(), "S3 access key");

        String secretKey =
            require(credentials.getSecretAccessKey(), "S3 secret key");

        builder.credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)));

        configureRegion(builder, credentials);
        configureEndpoint(builder, credentials);
        configureS3(builder, credentials);

    }

    /**
     * Configures the AWS region.
     */
    private static <T extends S3BaseClientBuilder<?, ?>> void configureRegion(T builder, S3Credentials credentials) {
        String region = credentials.getRegion();

        if (region == null || region.isBlank()) {
            builder.region(Region.US_EAST_1);
            return;
        }

        try {
            builder.region(Region.of(region.trim()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 region: " + region, e);
        }
    }

    /**
     * Configures a custom S3 endpoint if specified.
     */
    private static <T extends S3BaseClientBuilder<?, ?>> void configureEndpoint(T builder, S3Credentials credentials) {
        String endpoint = credentials.getCustomEndpoint();

        if (endpoint == null || endpoint.isBlank()) {
            return;
        }

        try {
            builder.endpointOverride(URI.create(endpoint.trim()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 endpoint: " + endpoint, e);
        }
    }

    /**
     * Applies S3-specific client configuration.
     */
    private static <T extends S3BaseClientBuilder<?, ?>> void configureS3(T builder, S3Credentials credentials) {
        builder.serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(credentials.isForcePath())
                .build());
    }

    /**
     * Validates that the specified value is configured and not empty.
     */
    private static String require(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is not configured");
        }

        value = value.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        return value;
    }
}