package com.testomatio.reporter.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testomatio.reporter.client.http.HttpClient;
import com.testomatio.reporter.client.http.OkHttpClientImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OkHttpClientImplTest {

    private HttpClient httpClient;
    private AutoCloseable mockitoCloseable;

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    @Mock
    private ObjectMapper mockObjectMapper;

    private static final String TEST_URL = "https://api.testomat.io/test";
    private static final String REQUEST_BODY = "{\"title\":\"Test\"}";
    private static final String RESPONSE_BODY = "{\"uid\":\"test-uid-123\"}";

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        resetAllMocks();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetAllMocks();

        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }

    private void resetAllMocks() {
        reset(mockOkHttpClient, mockCall, mockResponse, mockResponseBody, mockObjectMapper);
    }

    @Test
    void constructor_ShouldCreateInstanceWithDefaults() {
        // When
        OkHttpClientImpl client = new OkHttpClientImpl();

        // Then
        assertNotNull(client);
    }

    @Test
    void post_WithSuccessfulResponse_ShouldReturnDeserializedObject() throws Exception {
        // Given
        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("uid", "test-uid-123");

        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                });
             MockedConstruction<ObjectMapper> mapperMock = mockConstruction(ObjectMapper.class,
                     (mock, context) -> {
                         when(mock.readValue(anyString(), eq(Map.class))).thenReturn(expectedResponse);
                     })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(RESPONSE_BODY);

            // When
            Map<String, String> result = httpClient.post(TEST_URL, REQUEST_BODY, Map.class);

            // Then
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void post_WithNullResponseType_ShouldReturnNull() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(RESPONSE_BODY);

            // When
            Object result = httpClient.post(TEST_URL, REQUEST_BODY, null);

            // Then
            assertNull(result);
        }
    }

    @Test
    void post_WithUnsuccessfulResponse_ShouldThrowIOException() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.code()).thenReturn(400);
            when(mockResponse.message()).thenReturn("Bad Request");
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn("Error details");

            // When & Then
            assertThrows(IOException.class, () -> httpClient.post(TEST_URL, REQUEST_BODY, Map.class));
        }
    }

    @Test
    void post_WithNullResponseBody_ShouldHandleGracefully() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.code()).thenReturn(500);
            when(mockResponse.message()).thenReturn("Internal Server Error");
            when(mockResponse.body()).thenReturn(null);

            // When & Then
            assertThrows(IOException.class, () -> httpClient.post(TEST_URL, REQUEST_BODY, Map.class));
        }
    }

    @Test
    void post_WithIOExceptionDuringCall_ShouldPropagateException() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenThrow(new IOException("Network error"));

            // When & Then
            assertThrows(IOException.class, () -> httpClient.post(TEST_URL, REQUEST_BODY, Map.class));
        }
    }

    @Test
    void put_WithSuccessfulResponse_ShouldReturnDeserializedObject() throws Exception {
        // Given
        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("status", "updated");

        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                });
             MockedConstruction<ObjectMapper> mapperMock = mockConstruction(ObjectMapper.class,
                     (mock, context) -> {
                         when(mock.readValue(anyString(), eq(Map.class))).thenReturn(expectedResponse);
                     })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(RESPONSE_BODY);

            // When
            Map<String, String> result = httpClient.put(TEST_URL, REQUEST_BODY, Map.class);

            // Then
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void put_WithNullResponseType_ShouldReturnNull() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(RESPONSE_BODY);

            // When
            Object result = httpClient.put(TEST_URL, REQUEST_BODY, null);

            // Then
            assertNull(result);
        }
    }

    @Test
    void put_WithUnsuccessfulResponse_ShouldThrowIOException() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.code()).thenReturn(404);
            when(mockResponse.message()).thenReturn("Not Found");
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn("Resource not found");

            // When & Then
            assertThrows(IOException.class, () -> httpClient.put(TEST_URL, REQUEST_BODY, Map.class));
        }
    }

    @Test
    void put_WithIOExceptionDuringCall_ShouldPropagateException() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenThrow(new IOException("Connection timeout"));

            // When & Then
            assertThrows(IOException.class, () -> httpClient.put(TEST_URL, REQUEST_BODY, Map.class));
        }
    }

    @Test
    void post_WithStringResponseType_ShouldReturnString() throws Exception {
        // Given
        String expectedResponse = "Success";

        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                });
             MockedConstruction<ObjectMapper> mapperMock = mockConstruction(ObjectMapper.class,
                     (mock, context) -> {
                         when(mock.readValue(anyString(), eq(String.class))).thenReturn(expectedResponse);
                     })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn("\"Success\"");

            // When
            String result = httpClient.post(TEST_URL, REQUEST_BODY, String.class);

            // Then
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void put_WithCustomObjectType_ShouldDeserializeCorrectly() throws Exception {
        // Given
        TestResponse expectedResponse = new TestResponse("test-id", "completed");

        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                });
             MockedConstruction<ObjectMapper> mapperMock = mockConstruction(ObjectMapper.class,
                     (mock, context) -> {
                         when(mock.readValue(anyString(), eq(TestResponse.class))).thenReturn(expectedResponse);
                     })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn("{\"id\":\"test-id\",\"status\":\"completed\"}");

            // When
            TestResponse result = httpClient.put(TEST_URL, REQUEST_BODY, TestResponse.class);

            // Then
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void post_WithEmptyResponseBody_ShouldHandleGracefully() throws Exception {
        // Given
        try (MockedConstruction<OkHttpClient> clientMock = mockConstruction(OkHttpClient.class,
                (mock, context) -> {
                    when(mock.newCall(any(Request.class))).thenReturn(mockCall);
                })) {

            httpClient = new OkHttpClientImpl();

            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn("");

            // When
            Object result = httpClient.post(TEST_URL, REQUEST_BODY, null);

            // Then
            assertNull(result);
        }
    }

    // Helper class for testing custom object deserialization
    public static class TestResponse {
        private String id;
        private String status;

        public TestResponse() {
        }

        public TestResponse(String id, String status) {
            this.id = id;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestResponse that = (TestResponse) obj;
            return java.util.Objects.equals(id, that.id) &&
                    java.util.Objects.equals(status, that.status);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, status);
        }
    }
}