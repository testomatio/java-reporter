package io.testomat.core.artifact.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.testomat.core.artifact.client.AwsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URL;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtifactUrlGenerator Tests")
class ArtifactUrlGeneratorTest {

    @Mock
    private AwsClient awsClient;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private S3Presigner s3Presigner;

    private ArtifactUrlGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ArtifactUrlGenerator(awsClient, s3Presigner);
    }

    @Test
    @DisplayName("Should generate URL for given bucket and key")
    void testGenerateUrl() throws Exception {
        String bucket = "test-bucket";
        String key = "test-key/file.txt";
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/test-key/file.txt");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(expectedUrl);

        String actualUrl = generator.generateUrl(bucket, key);

        assertNotNull(actualUrl);
        assertEquals(expectedUrl.toString(), actualUrl);
    }

    @Test
    @DisplayName("Should call S3 utilities with correct request")
    void testCallsS3UtilitiesWithCorrectRequest() throws Exception {
        String bucket = "my-bucket";
        String key = "artifacts/test.png";
        URL mockUrl = new URL("https://my-bucket.s3.amazonaws.com/artifacts/test.png");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        generator.generateUrl(bucket, key);

        verify(awsClient).getS3Client();
        verify(s3Client).utilities();
        verify(s3Utilities).getUrl(any(GetUrlRequest.class));
    }

    @Test
    @DisplayName("Should handle bucket names with special characters")
    void testHandleBucketWithSpecialCharacters() throws Exception {
        String bucket = "test-bucket-123";
        String key = "file.txt";
        URL mockUrl = new URL("https://test-bucket-123.s3.amazonaws.com/file.txt");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertNotNull(url);
        assertTrue(url.contains("test-bucket-123"));
    }

    @Test
    @DisplayName("Should handle keys with nested paths")
    void testHandleKeysWithNestedPaths() throws Exception {
        String bucket = "artifacts";
        String key = "run-123/TestName::rid-456/screenshot.png";
        URL mockUrl = new URL("https://artifacts.s3.amazonaws.com/run-123/TestName::rid-456/screenshot.png");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertNotNull(url);
        assertTrue(url.contains("run-123"));
        assertTrue(url.contains("screenshot.png"));
    }

    @Test
    @DisplayName("Should handle keys with special characters")
    void testHandleKeysWithSpecialCharacters() throws Exception {
        String bucket = "my-bucket";
        String key = "path/with spaces/file name.txt";
        URL mockUrl = new URL("https://my-bucket.s3.amazonaws.com/path/with%20spaces/file%20name.txt");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertNotNull(url);
    }

    @Test
    @DisplayName("Should return URL as string")
    void testReturnUrlAsString() throws Exception {
        String bucket = "test-bucket";
        String key = "test-key";
        String expectedUrlString = "https://test-bucket.s3.amazonaws.com/test-key";
        URL mockUrl = new URL(expectedUrlString);

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertEquals(expectedUrlString, url);
    }

    @Test
    @DisplayName("Should create generator with default constructor")
    void testDefaultConstructor() {
        ArtifactUrlGenerator defaultGenerator = new ArtifactUrlGenerator();

        assertNotNull(defaultGenerator);
    }

    @Test
    @DisplayName("Should create generator with custom AwsClient and S3Presigner")
    void testConstructorWithParameters() {
        AwsClient customAwsClient = mock(AwsClient.class);
        S3Presigner customPresigner = mock(S3Presigner.class);

        ArtifactUrlGenerator customGenerator = new ArtifactUrlGenerator(customAwsClient, customPresigner);

        assertNotNull(customGenerator);
    }

    @Test
    @DisplayName("Should handle multiple URL generation calls")
    void testMultipleUrlGenerationCalls() throws Exception {
        String bucket1 = "bucket1";
        String key1 = "key1";
        String bucket2 = "bucket2";
        String key2 = "key2";

        URL mockUrl1 = new URL("https://bucket1.s3.amazonaws.com/key1");
        URL mockUrl2 = new URL("https://bucket2.s3.amazonaws.com/key2");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(mockUrl1)
                .thenReturn(mockUrl2);

        String url1 = generator.generateUrl(bucket1, key1);
        String url2 = generator.generateUrl(bucket2, key2);

        assertNotNull(url1);
        assertNotNull(url2);
        assertTrue(url1.contains("bucket1"));
        assertTrue(url2.contains("bucket2"));
    }

    @Test
    @DisplayName("Should generate URL for image files")
    void testGenerateUrlForImageFiles() throws Exception {
        String bucket = "images";
        String key = "screenshots/test.png";
        URL mockUrl = new URL("https://images.s3.amazonaws.com/screenshots/test.png");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertNotNull(url);
        assertTrue(url.endsWith("test.png"));
    }

    @Test
    @DisplayName("Should generate URL for log files")
    void testGenerateUrlForLogFiles() throws Exception {
        String bucket = "logs";
        String key = "test-run/error.log";
        URL mockUrl = new URL("https://logs.s3.amazonaws.com/test-run/error.log");

        when(awsClient.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(mockUrl);

        String url = generator.generateUrl(bucket, key);

        assertNotNull(url);
        assertTrue(url.endsWith("error.log"));
    }
}
