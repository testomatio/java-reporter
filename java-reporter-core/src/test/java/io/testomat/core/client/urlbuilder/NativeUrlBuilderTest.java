package io.testomat.core.client.urlbuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.testomat.core.exception.InvalidProvidedPropertyException;
import io.testomat.core.exception.UrlBuildingException;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NativeUrlBuilder Tests")
class NativeUrlBuilderTest {

    private NativeUrlBuilder urlBuilder;
    private PropertyProvider mockPropertyProvider;
    private PropertyProviderFactory mockProviderFactory;

    @BeforeEach
    void setUp() {
        mockPropertyProvider = mock(PropertyProvider.class);
        mockProviderFactory = mock(PropertyProviderFactory.class);
        when(mockProviderFactory.getPropertyProvider()).thenReturn(mockPropertyProvider);
    }

    @Test
    @DisplayName("Should build create run URL successfully")
    void buildCreateRunUrl_ValidProperties_ShouldReturnCorrectUrl() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("test-api-key");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildCreateRunUrl();

            assertNotNull(result);
            assertTrue(result.contains("https://api.testomat.io"));
            assertTrue(result.contains("/api/reporter"));
            assertTrue(result.contains("api_key=test-api-key"));
            assertTrue(result.contains("?"));
        }
    }

    @Test
    @DisplayName("Should handle URL with trailing slash")
    void buildCreateRunUrl_UrlWithTrailingSlash_ShouldNormalizeUrl() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io/");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("api-key");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildCreateRunUrl();

            assertFalse(result.contains("//api/reporter"));
            assertTrue(result.contains("/api/reporter"));
        }
    }

    @Test
    @DisplayName("Should URL encode API key")
    void buildCreateRunUrl_SpecialCharactersInApiKey_ShouldEncodeCorrectly() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("key with spaces & special chars");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildCreateRunUrl();

            assertTrue(result.contains("key+with+spaces"));
            assertTrue(result.contains("%26"));
            assertFalse(result.contains(" "));
        }
    }

    @Test
    @DisplayName("Should throw exception for null base URL")
    void buildCreateRunUrl_NullBaseUrl_ShouldThrowException() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn(null);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            InvalidProvidedPropertyException exception = assertThrows(
                    InvalidProvidedPropertyException.class,
                    () -> urlBuilder.buildCreateRunUrl()
            );
            assertTrue(exception.getMessage().contains("Base URL is required"));
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid protocol")
    void buildCreateRunUrl_InvalidProtocol_ShouldThrowException() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("ftp://invalid.com");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            InvalidProvidedPropertyException exception = assertThrows(
                    InvalidProvidedPropertyException.class,
                    () -> urlBuilder.buildCreateRunUrl()
            );
            assertTrue(exception.getMessage().contains("must start with http://"));
        }
    }

    @Test
    @DisplayName("Should throw exception for null API key")
    void buildCreateRunUrl_NullApiKey_ShouldThrowException() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn(null);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            InvalidProvidedPropertyException exception = assertThrows(
                    InvalidProvidedPropertyException.class,
                    () -> urlBuilder.buildCreateRunUrl()
            );
            assertTrue(exception.getMessage().contains("API key is required"));
        }
    }

    @Test
    @DisplayName("Should build report test URL successfully")
    void buildReportTestUrl_ValidInput_ShouldReturnCorrectUrl() {
        String testRunUid = "run-123";
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("test-key");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildReportTestUrl(testRunUid);

            assertNotNull(result);
            assertTrue(result.contains("/" + testRunUid + "/testrun"));
            assertTrue(result.contains("api_key=test-key"));
        }
    }

    @Test
    @DisplayName("Should throw exception for null test run UID")
    void buildReportTestUrl_NullTestRunUid_ShouldThrowException() {
        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            UrlBuildingException exception = assertThrows(
                    UrlBuildingException.class,
                    () -> urlBuilder.buildReportTestUrl(null)
            );
            assertTrue(exception.getMessage().contains("Test run id is null or empty"));
        }
    }

    @Test
    @DisplayName("Should throw exception for empty test run UID")
    void buildReportTestUrl_EmptyTestRunUid_ShouldThrowException() {
        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            assertThrows(UrlBuildingException.class, () -> urlBuilder.buildReportTestUrl(""));
            assertThrows(UrlBuildingException.class, () -> urlBuilder.buildReportTestUrl("   "));
        }
    }

    @Test
    @DisplayName("Should trim whitespace from test run UID")
    void buildReportTestUrl_WhitespaceInUid_ShouldTrimCorrectly() {
        String testRunUid = "  run-123  ";
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("test-key");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildReportTestUrl(testRunUid);

            assertTrue(result.contains("/run-123/testrun"));
            assertFalse(result.contains("  run-123  "));
        }
    }

    @Test
    @DisplayName("Should build finish test run URL successfully")
    void buildFinishTestRunUrl_ValidInput_ShouldReturnCorrectUrl() {
        String testRunUid = "run-456";
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://api.testomat.io");
        when(mockPropertyProvider.getProperty("testomatio.api.key")).thenReturn("finish-key");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();
            String result = urlBuilder.buildFinishTestRunUrl(testRunUid);

            assertNotNull(result);
            assertTrue(result.contains("/" + testRunUid));
            assertTrue(result.contains("api_key=finish-key"));
            assertFalse(result.contains("/testrun")); // finish URL doesn't have testrun path
        }
    }

    @Test
    @DisplayName("Should validate hostname in base URL")
    void buildCreateRunUrl_MissingHostname_ShouldThrowException() {
        when(mockPropertyProvider.getProperty("testomatio.url")).thenReturn("https://");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic = 
                 mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockProviderFactory);

            urlBuilder = new NativeUrlBuilder();

            InvalidProvidedPropertyException exception = assertThrows(
                    InvalidProvidedPropertyException.class,
                    () -> urlBuilder.buildCreateRunUrl()
            );
            // The exception message could be "Malformed base URL" or "must contain valid hostname"
            assertTrue(exception.getMessage().contains("hostname") || 
                      exception.getMessage().contains("Malformed") ||
                      exception.getMessage().contains("valid"));
        }
    }
}