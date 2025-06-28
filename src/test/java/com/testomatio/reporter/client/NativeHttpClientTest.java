package com.testomatio.reporter.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.testomatio.reporter.client.http.NativeHttpClient;
import com.testomatio.reporter.exception.RequestExecutionFailedException;
import com.testomatio.reporter.exception.RequestStatusNotSuccessException;
import com.testomatio.reporter.exception.RequestUriBuildingException;
import com.testomatio.reporter.exception.ResponseJsonParsingException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NativeHttpClientTest {

    private WireMockServer wireMockServer;
    private NativeHttpClient nativeHttpClient;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(0));
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());

        baseUrl = "http://localhost:" + wireMockServer.port();
        nativeHttpClient = new NativeHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
            wireMockServer.stop();
        }
    }

    @Test
    void constructor_ShouldInitializeSuccessfully() {
        assertDoesNotThrow(() -> new NativeHttpClient());
    }

    @Test
    void post_WithValidRequest_ShouldReturnParsedResponse() {
        // Given
        String url = baseUrl + "/api/test";
        String requestBody = "{\"key\":\"value\"}";
        String responseJson = "{\"id\":\"123\",\"status\":\"success\"}";

        stubFor(post(urlEqualTo("/api/test"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        // When
        Map result = nativeHttpClient.post(url, requestBody, Map.class);

        // Then
        assertNotNull(result);
        assertEquals("123", result.get("id"));
        assertEquals("success", result.get("status"));

        // Verify request was made
        verify(postRequestedFor(urlEqualTo("/api/test"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void post_WithNullResponseType_ShouldReturnNull() {
        // Given
        String url = baseUrl + "/api/test";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"result\":\"ok\"}")));

        // When
        Object result = nativeHttpClient.post(url, requestBody, null);

        // Then
        assertNull(result);
    }

    @Test
    void put_WithValidRequest_ShouldReturnParsedResponse() {
        // Given
        String url = baseUrl + "/api/update";
        String requestBody = "{\"id\":\"123\",\"name\":\"updated\"}";
        String responseJson = "{\"updated\":true}";

        stubFor(put(urlEqualTo("/api/update"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        // When
        Map result = nativeHttpClient.put(url, requestBody, Map.class);

        // Then
        assertNotNull(result);
        assertEquals(true, result.get("updated"));

        verify(putRequestedFor(urlEqualTo("/api/update"))
                .withRequestBody(equalTo(requestBody))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void post_WithInvalidUrl_ShouldThrowRequestUriBuildingException() {
        // Given
        String invalidUrl = "not a valid url";
        String requestBody = "{\"key\":\"value\"}";

        // When & Then
        RequestUriBuildingException exception = assertThrows(
                RequestUriBuildingException.class,
                () -> nativeHttpClient.post(invalidUrl, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("Failed to build URI from url"));
        assertTrue(exception.getMessage().contains(invalidUrl));
    }

    @Test
    void post_WithServerError_ShouldThrowRequestStatusNotSuccessException() {
        // Given
        String url = baseUrl + "/api/error";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/error"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // When & Then
        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("API request returned status code 500"));
    }

    @Test
    void post_WithClientError_ShouldThrowRequestStatusNotSuccessException() {
        // Given
        String url = baseUrl + "/api/notfound";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/notfound"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        // When & Then
        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("API request returned status code 404"));
    }

    @Test
    void post_WithRedirectStatus_ShouldThrowRequestStatusNotSuccessException() {
        // Given
        String url = baseUrl + "/api/redirect";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/redirect"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Location", "/new-location")));

        // When & Then
        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );
        assertInstanceOf(RequestStatusNotSuccessException.class, exception);
        assertNotNull(exception.getMessage());
    }

    @Test
    void post_WithInvalidJson_ShouldThrowResponseJsonParsingException() {
        // Given
        String url = baseUrl + "/api/invalid-json";
        String requestBody = "{\"key\":\"value\"}";
        String invalidJson = "{invalid json}";

        stubFor(post(urlEqualTo("/api/invalid-json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(invalidJson)));

        // When & Then
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("Failed to parse response json"));
        assertTrue(exception.getMessage().contains(invalidJson));
    }

    @Test
    void post_WithEmptyResponse_ShouldThrowResponseJsonParsingException() {
        // Given
        String url = baseUrl + "/api/empty";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/empty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("")));

        // When & Then
        ResponseJsonParsingException exception = assertThrows(
                ResponseJsonParsingException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("Failed to parse response json"));
    }

    @Test
    void post_WithConnectionTimeout_ShouldThrowRequestTimeoutException() {
        // Given
        String url = "http://10.255.255.1:12345/timeout"; // Non-routable IP
        String requestBody = "{\"key\":\"value\"}";

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> nativeHttpClient.post(url, requestBody, Map.class)
        );

        assertTrue(exception instanceof RequestExecutionFailedException ||
                exception.getClass().getSimpleName().contains("Timeout"));
    }

    @Test
    void post_WithSuccessfulStatusCodes_ShouldWork() {
        // Test various 2xx status codes
        int[] successCodes = {200, 201, 202};

        for (int statusCode : successCodes) {
            String url = baseUrl + "/api/status/" + statusCode;
            String requestBody = "{\"key\":\"value\"}";
            String responseBody = "{\"status\":\"ok\"}";

            stubFor(post(urlEqualTo("/api/status/" + statusCode))
                    .willReturn(aResponse()
                            .withStatus(statusCode)
                            .withBody(responseBody)));

            // Expecting parsed response
            assertDoesNotThrow(() ->
                    nativeHttpClient.post(url, requestBody, Map.class)
            );
        }
    }

    @Test
    void post_WithNoContentStatus_ShouldReturnNull() {
        String url = baseUrl + "/api/status/204";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/status/204"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withBody(""))); // Empty body for 204

        Object result = nativeHttpClient.post(url, requestBody, null);
        assertNull(result);
    }

    @Test
    void post_WithDifferentContentTypes_ShouldAlwaysUseApplicationJson() {
        // Given
        String url = baseUrl + "/api/content-type";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(post(urlEqualTo("/api/content-type"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"result\":\"ok\"}")));

        // When
        nativeHttpClient.post(url, requestBody, Map.class);

        // Then
        verify(postRequestedFor(urlEqualTo("/api/content-type"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void put_WithServerError_ShouldThrowRequestStatusNotSuccessException() {
        // Given
        String url = baseUrl + "/api/put-error";
        String requestBody = "{\"key\":\"value\"}";

        stubFor(put(urlEqualTo("/api/put-error"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Server Error")));

        // When & Then
        RequestStatusNotSuccessException exception = assertThrows(
                RequestStatusNotSuccessException.class,
                () -> nativeHttpClient.put(url, requestBody, Map.class)
        );

        assertTrue(exception.getMessage().contains("API request returned status code 500"));
    }

    // Custom test class for JSON parsing
    public static class TestResponse {
        private String id;
        private String message;

        public TestResponse() {
        }

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }
    }

    @Test
    void post_WithCustomClass_ShouldDeserializeCorrectly() {
        // Given
        String url = baseUrl + "/api/custom";
        String requestBody = "{\"key\":\"value\"}";
        String responseJson = "{\"id\":\"test-id\",\"message\":\"test-message\"}";

        stubFor(post(urlEqualTo("/api/custom"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(responseJson)));

        // When
        TestResponse result = nativeHttpClient.post(url, requestBody, TestResponse.class);

        // Then
        assertNotNull(result);
        assertEquals("test-id", result.getId());
        assertEquals("test-message", result.getMessage());
    }
}