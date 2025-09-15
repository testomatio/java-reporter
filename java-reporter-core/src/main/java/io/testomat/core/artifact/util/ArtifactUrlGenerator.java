package io.testomat.core.artifact.util;

import io.testomat.core.artifact.client.AwsClient;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class ArtifactUrlGenerator {
    private final AwsClient awsClient;
    private final S3Presigner s3Presigner;


    public ArtifactUrlGenerator() {
        this.awsClient = new AwsClient();
        this.s3Presigner = S3Presigner.create();
    }

    public ArtifactUrlGenerator(AwsClient awsClient, S3Presigner s3Presigner) {
        this.awsClient = awsClient;
        this.s3Presigner = s3Presigner;
    }

    public String generateUrl(String bucket, String key) {

        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return awsClient.getS3Client().utilities().getUrl(request).toString();
    }
}
