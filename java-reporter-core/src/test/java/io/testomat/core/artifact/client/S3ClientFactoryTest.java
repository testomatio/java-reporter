package io.testomat.core.artifact.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.testomat.core.facade.methods.artifact.client.S3ClientFactory;
import io.testomat.core.facade.methods.artifact.credential.S3Credentials;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

class S3ClientFactoryTest {

    private S3ClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new S3ClientFactory();
    }

    @Test
    void shouldReturnDefaultRegionWhenRegionIsBlank() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getRegion()).thenReturn(" ");
        assertEquals(Region.US_EAST_1, invokeResolveRegion(s3));
    }

    @Test
    void shouldReturnProvidedRegion() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getRegion()).thenReturn("eu-central-1");
        assertEquals(Region.EU_CENTRAL_1, invokeResolveRegion(s3));
    }

    @Test
    void shouldCreateBasicCredentials() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getAccessKeyId()).thenReturn("access-key");
        when(s3.getSecretAccessKey()).thenReturn("secret-key");

        AwsCredentialsProvider provider = invokeBuildBaseProvider(s3);
        assertInstanceOf(StaticCredentialsProvider.class, provider);
        AwsBasicCredentials creds = (AwsBasicCredentials) provider.resolveCredentials();
        assertEquals("access-key", creds.accessKeyId());
        assertEquals("secret-key", creds.secretAccessKey());
    }

    @Test
    void shouldTrimBasicCredentials() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getAccessKeyId()).thenReturn("  access-key  ");
        when(s3.getSecretAccessKey()).thenReturn("  secret-key  ");

        StaticCredentialsProvider provider = (StaticCredentialsProvider) invokeBuildBaseProvider(s3);
        assertEquals("access-key", provider.resolveCredentials().accessKeyId());
        assertEquals("secret-key", provider.resolveCredentials().secretAccessKey());
    }

    @Test
    void shouldCreateSessionCredentials() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getAccessKeyId()).thenReturn("ASIAkey");
        when(s3.getSecretAccessKey()).thenReturn("secret");
        when(s3.getSessionToken()).thenReturn("token");

        StaticCredentialsProvider provider = (StaticCredentialsProvider) invokeBuildBaseProvider(s3);
        assertInstanceOf(AwsSessionCredentials.class, provider.resolveCredentials());
        AwsSessionCredentials creds = (AwsSessionCredentials) provider.resolveCredentials();
        assertEquals("ASIAkey", creds.accessKeyId());
        assertEquals("token", creds.sessionToken());
    }

    @Test
    void shouldFallbackToDefaultProviderWhenNoKeys() throws Exception {
        S3Credentials s3 = mock(S3Credentials.class);
        assertInstanceOf(DefaultCredentialsProvider.class, invokeBuildBaseProvider(s3));
    }

    @Test
    void shouldConfigureCustomEndpoint() throws Exception {
        S3ClientBuilder builder = mock(S3ClientBuilder.class, RETURNS_SELF);
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getCustomEndpoint()).thenReturn("http://localhost:9000");
        when(s3.isForcePath()).thenReturn(false);

        invokeConfigureEndpoint(builder, s3);

        verify(builder).endpointOverride(any());
        verify(builder).serviceConfiguration(any(S3Configuration.class));
    }

    @Test
    void shouldConfigurePathStyleWhenForcePathEnabled() throws Exception {
        S3ClientBuilder builder = mock(S3ClientBuilder.class, RETURNS_SELF);
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getCustomEndpoint()).thenReturn(null);
        when(s3.isForcePath()).thenReturn(true);

        invokeConfigureEndpoint(builder, s3);

        verify(builder).serviceConfiguration(any(S3Configuration.class));
    }

    @Test
    void shouldThrowExceptionForInvalidEndpoint() {
        S3ClientBuilder builder = mock(S3ClientBuilder.class, RETURNS_SELF);
        S3Credentials s3 = mock(S3Credentials.class);
        when(s3.getCustomEndpoint()).thenReturn("invalid-url%%%");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> invokeConfigureEndpoint(builder, s3)
        );
        assertTrue(exception.getMessage().contains("Invalid endpoint URL"));
    }

    private Region invokeResolveRegion(S3Credentials s3) throws Exception {
        Method method = S3ClientFactory.class.getDeclaredMethod(
            "resolveRegion", S3Credentials.class);
        method.setAccessible(true);
        try {
            return (Region) method.invoke(factory, s3);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private AwsCredentialsProvider invokeBuildBaseProvider(S3Credentials s3) throws Exception {
        Method method = S3ClientFactory.class.getDeclaredMethod(
            "buildBaseProvider", S3Credentials.class);
        method.setAccessible(true);
        try {
            return (AwsCredentialsProvider) method.invoke(factory, s3);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private void invokeConfigureEndpoint(
        S3ClientBuilder builder, S3Credentials s3
    ) throws Exception {
        Method method = S3ClientFactory.class.getDeclaredMethod(
            "configureEndpoint", S3ClientBuilder.class, S3Credentials.class);
        method.setAccessible(true);
        try {
            method.invoke(factory, builder, s3);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
