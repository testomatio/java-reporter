package io.testomat.core.facade.methods.artifact.client;

import io.testomat.core.facade.methods.artifact.credential.CredentialsManager;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import java.net.URI;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

/**
 * Factory for creating configured S3Client instances.
 */
public class S3ClientFactory {

    /**
     * Creates configured S3 client.
     */
    public S3Client createS3Client() {
        S3Credentials s3 = CredentialsManager.getCredentials();

        Region region = resolveRegion(s3);

        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(buildCredentialsProvider(s3, region))
                .region(region);

        configureEndpoint(builder, s3);

        return builder.build();
    }

    /**
     * Creates credentials provider.
     *
     * Supports:
     * - AccessKey + SecretKey
     * - AccessKey + SecretKey + SessionToken
     * - IAM role assumption via STS (isIam + roleArn)
     */
    private AwsCredentialsProvider buildCredentialsProvider(S3Credentials s3, Region region) {

        if (!isBlank(s3.getAccessKeyId())
                && !isBlank(s3.getSecretAccessKey())
                && !isBlank(s3.getSessionToken())) {
            return StaticCredentialsProvider.create(
                AwsSessionCredentials.create(
                    s3.getAccessKeyId().trim(),
                    s3.getSecretAccessKey().trim(),
                    s3.getSessionToken().trim()
                )
            );
        }

        if (s3.isIam() && !isBlank(s3.getRoleArn())) {
            AwsCredentialsProvider baseProvider = buildBaseProvider(s3);
            StsClient stsClient = StsClient.builder()
                    .credentialsProvider(baseProvider)
                    .region(region)
                    .build();

            return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(r -> {
                    r.roleArn(s3.getRoleArn().trim());
                    r.roleSessionName("testomat-s3-session");
                    if (!isBlank(s3.getExternalId())) {
                        r.externalId(s3.getExternalId().trim());
                    }
                })
                .build();
        }

        return buildBaseProvider(s3);
    }

    private AwsCredentialsProvider buildBaseProvider(S3Credentials s3) {
        if (!isBlank(s3.getAccessKeyId()) && !isBlank(s3.getSecretAccessKey())) {
            if (!isBlank(s3.getSessionToken())) {
                return StaticCredentialsProvider.create(
                    AwsSessionCredentials.create(
                        s3.getAccessKeyId().trim(),
                        s3.getSecretAccessKey().trim(),
                        s3.getSessionToken().trim()
                    )
                );
            }
            return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    s3.getAccessKeyId().trim(),
                    s3.getSecretAccessKey().trim()
                )
            );
        }
        return DefaultCredentialsProvider.create();
    }

    /**
     * Resolves AWS region.
     */
    private Region resolveRegion(S3Credentials s3Credentials) {
        try {
            if (isBlank(s3Credentials.getRegion())) {
                return Region.US_EAST_1;
            }

            return Region.of(s3Credentials.getRegion().trim());

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid AWS region: "
                + s3Credentials.getRegion(), e);
        }
    }

    /**
     * Configures endpoint and path style access.
     */
    private void configureEndpoint(S3ClientBuilder builder, S3Credentials s3Credentials) {
        boolean hasCustomEndpoint = !isBlank(s3Credentials.getCustomEndpoint());

        if (hasCustomEndpoint) {
            try {
                builder.endpointOverride(URI.create(s3Credentials.getCustomEndpoint().trim()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid endpoint URL: "
                    + s3Credentials.getCustomEndpoint(), e);
            }
        }

        if (s3Credentials.isForcePath() || hasCustomEndpoint) {
            builder.serviceConfiguration(
                    S3Configuration.builder()
                    .pathStyleAccessEnabled(s3Credentials.isForcePath())
                    .build());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
