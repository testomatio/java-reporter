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
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

/**
 * Factory for creating configured S3Client instances with custom endpoint support.
 * Handles AWS credentials, regions, and S3-compatible storage configurations.
 */
public class S3ClientFactory {

    /**
     * Creates a configured S3Client based on current credentials and settings.
     * Priority:
     * 1. IAM Role (if roleArn configured)
     * 2. Static access key / secret key
     *
     * @return configured S3Client instance
     * @throws IllegalArgumentException if credentials are invalid or missing
     */
    public S3Client createS3Client() {
        S3Credentials s3 = CredentialsManager.getCredentials();

        Region region = resolveRegion(s3);

        AwsCredentialsProvider provider =
            buildCredentialsProvider(s3, region);

        S3ClientBuilder builder = S3Client.builder()
            .credentialsProvider(provider)
            .region(region);

        configureEndpoint(builder, s3);

        return builder.build();
    }

    /**
     * Builds AWS credentials provider.
     */
    private AwsCredentialsProvider buildCredentialsProvider(S3Credentials s3, Region region) {
        boolean useIamRole = s3.getRoleArn() != null && !s3.getRoleArn().isBlank();

        if (useIamRole) {
            return buildIamRoleProvider(s3, region);
        }

        return buildStaticCredentialsProvider(s3);
    }

    /**
     * Creates AssumeRole credentials provider.
     */
    private AwsCredentialsProvider buildIamRoleProvider(S3Credentials s3, Region region) {
        StsClient stsClient = StsClient.builder()
            .region(region)
            .build();

        return StsAssumeRoleCredentialsProvider.builder()
            .stsClient(stsClient)
            .refreshRequest(request -> {
                request.roleArn(s3.getRoleArn().trim());
                request.roleSessionName("testomat-s3-upload");

                if (s3.getExternalId() != null && !s3.getExternalId().isBlank()) {
                    request.externalId(s3.getExternalId().trim());
                }
            }).build();
    }

    /**
     * Creates static credentials provider.
     */
    private AwsCredentialsProvider buildStaticCredentialsProvider(S3Credentials s3) {

        if (s3.getAccessKeyId() == null || s3.getAccessKeyId().isBlank()) {
            throw new IllegalArgumentException("AWS access key is missing");
        }

        if (s3.getSecretAccessKey() == null || s3.getSecretAccessKey().isBlank()) {
            throw new IllegalArgumentException("AWS secret key is missing");
        }

        AwsBasicCredentials credentials =
            AwsBasicCredentials.create(
                s3.getAccessKeyId().trim(),
                s3.getSecretAccessKey().trim());

        return StaticCredentialsProvider.create(credentials);
    }

    /**
     * Resolves AWS region.
     */
    private Region resolveRegion(S3Credentials s3) {
        try {
            if (s3.getRegion() == null || s3.getRegion().isBlank()) {
                return Region.US_EAST_1;
            }
            return Region.of(s3.getRegion().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid AWS region: " + s3.getRegion(), e);
        }
    }

    /**
     * Configures custom endpoint and path-style access.
     */
    private void configureEndpoint(S3ClientBuilder builder, S3Credentials s3) {
        boolean hasCustomEndpoint = s3.getCustomEndpoint() != null && !s3.getCustomEndpoint().isBlank();

        if (hasCustomEndpoint) {
            try {
                builder.endpointOverride(URI.create(s3.getCustomEndpoint().trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid endpoint URL: " + s3.getCustomEndpoint(), e);
            }
        }

        if (s3.isForcePath() || hasCustomEndpoint) {
            builder.serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(
                        s3.isForcePath()
                    )
                    .build()
            );
        }
    }
}