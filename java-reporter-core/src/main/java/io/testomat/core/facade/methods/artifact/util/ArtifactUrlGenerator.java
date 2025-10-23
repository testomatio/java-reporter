package io.testomat.core.facade.methods.artifact.util;

import io.testomat.core.facade.methods.artifact.client.AwsClient;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Utility class for generating public and presigned URLs for S3 artifacts.
 * Supports both direct S3 URLs and time-limited presigned URLs.
 */
public class ArtifactUrlGenerator {
    private final AwsClient awsClient;


    public ArtifactUrlGenerator() {
        this.awsClient = new AwsClient();
    }

    public ArtifactUrlGenerator(AwsClient awsClient, S3Presigner s3Presigner) {
        this.awsClient = awsClient;
    }

    public String generateUrl(String bucket, String key) {

        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return awsClient.getS3Client().utilities().getUrl(request).toString();
    }
}
