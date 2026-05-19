package io.testomat.core.facade.methods.artifact.client;

import static io.testomat.core.constants.ArtifactPropertyNames.ARTIFACT_MAX_CONCURRENCY;

import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;

/**
 * Factory for creating configured asynchronous S3 clients.
 */
public class S3AsyncClientFactory {

    private final PropertyProviderFactory factory =
        PropertyProviderFactoryImpl.getPropertyProviderFactory();
    private final PropertyProvider provider = factory.getPropertyProvider();

    /**
     * Maximum number of concurrent HTTP requests.
     */
    private final int MAX_CONCURRENCY =
        Integer.parseInt(provider.getProperty(ARTIFACT_MAX_CONCURRENCY));

    /**
     * Creates a configured asynchronous S3 client.
     */
    public S3AsyncClient createS3AsyncClient() {
        S3AsyncClientBuilder builder = S3AsyncClient.builder();
        S3ClientConfiguration.configure(builder);
        builder.httpClientBuilder(
            NettyNioAsyncHttpClient.builder()
                .maxConcurrency(MAX_CONCURRENCY)
        );

        return builder.build();
    }
}