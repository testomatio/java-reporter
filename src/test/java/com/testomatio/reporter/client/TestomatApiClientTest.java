package com.testomatio.reporter.client;

import com.testomatio.reporter.client.http.HttpClient;
import com.testomatio.reporter.client.request.TestomatRequestBodyBuilder;
import com.testomatio.reporter.client.util.RequestUrlBuilderUtil;
import com.testomatio.reporter.exception.FinishReportFailedException;
import com.testomatio.reporter.exception.ReportingFailedException;
import com.testomatio.reporter.exception.RunCreationFailedException;
import com.testomatio.reporter.model.TestRunResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.testomatio.reporter.constants.CommonConstants.RESPONSE_UID_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestomatApiClientTest {

    private TestomatApiClient testomatApiClient;
    private AutoCloseable mockitoCloseable;

    @Mock
    private HttpClient httpClient;

    @Mock
    private TestomatRequestBodyBuilder requestBodyBuilder;

    @Mock
    private TestRunResult testRunResult1;

    @Mock
    private TestRunResult testRunResult2;

    private static final String API_KEY = "test-api-key";
    private static final String TEST_RUN_UID = "test-run-uid-123";
    private static final String TEST_TITLE = "Test Run Title";
    private static final String TEST_URL = "https://api.testomat.io/test";
    private static final String REQUEST_BODY = "{\"title\":\"Test Run Title\"}";

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
        reset(httpClient, requestBodyBuilder, testRunResult1, testRunResult2);
    }

    @Test
    void constructor_WithApiKeyOnly_ShouldCreateClientWithDefaults() {
        // When
        TestomatApiClient client = new TestomatApiClient(API_KEY);

        // Then
        assertNotNull(client);
    }

    @Test
    void constructor_WithAllDependencies_ShouldCreateClient() {
        // When
        TestomatApiClient client = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        // Then
        assertNotNull(client);
    }

    @Test
    void createRun_WithValidTitle_ShouldReturnRunId() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        Map<String, String> response = new HashMap<>();
        response.put(RESPONSE_UID_KEY, TEST_RUN_UID);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(RequestUrlBuilderUtil::buildCreateRunUrl).thenReturn(TEST_URL);
            when(requestBodyBuilder.buildCreateRunBody(TEST_TITLE)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, Map.class)).thenReturn(response);

            // When
            String result = testomatApiClient.createRun(TEST_TITLE);

            // Then
            assertEquals(TEST_RUN_UID, result);
            verify(requestBodyBuilder).buildCreateRunBody(TEST_TITLE);
            verify(httpClient).post(TEST_URL, REQUEST_BODY, Map.class);
        }
    }

    @Test
    void createRun_WithNullResponse_ShouldThrowException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(RequestUrlBuilderUtil::buildCreateRunUrl).thenReturn(TEST_URL);
            when(requestBodyBuilder.buildCreateRunBody(TEST_TITLE)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, Map.class)).thenReturn(null);

            // When & Then
            assertThrows(RunCreationFailedException.class,
                    () -> testomatApiClient.createRun(TEST_TITLE));
        }
    }

    @Test
    void createRun_WithResponseMissingUid_ShouldThrowException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        Map<String, String> response = new HashMap<>();
        // Response without RESPONSE_UID_KEY

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(RequestUrlBuilderUtil::buildCreateRunUrl).thenReturn(TEST_URL);
            when(requestBodyBuilder.buildCreateRunBody(TEST_TITLE)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, Map.class)).thenReturn(response);

            // When & Then
            assertThrows(RunCreationFailedException.class,
                    () -> testomatApiClient.createRun(TEST_TITLE));
        }
    }

    @Test
    void createRun_WithIOException_ShouldPropagateException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(RequestUrlBuilderUtil::buildCreateRunUrl).thenReturn(TEST_URL);
            when(requestBodyBuilder.buildCreateRunBody(TEST_TITLE)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, Map.class))
                    .thenThrow(new IOException("Network error"));

            // When & Then
            assertThrows(IOException.class, () -> testomatApiClient.createRun(TEST_TITLE));
        }
    }

    @Test
    void reportTest_WithValidResult_ShouldCallHttpClient() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildReportTestUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(testRunResult1.getTestId()).thenReturn("test-id-1");
            when(requestBodyBuilder.buildSingleTestReportBody(testRunResult1)).thenReturn(REQUEST_BODY);

            // When
            testomatApiClient.reportTest(TEST_RUN_UID, testRunResult1);

            // Then
            verify(requestBodyBuilder).buildSingleTestReportBody(testRunResult1);
            verify(httpClient).post(TEST_URL, REQUEST_BODY, null);
        }
    }

    @Test
    void reportTest_WithHttpException_ShouldThrowReportingFailedException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildReportTestUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(testRunResult1.getTestId()).thenReturn("test-id-1");
            when(requestBodyBuilder.buildSingleTestReportBody(testRunResult1)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, null))
                    .thenThrow(new IOException("Network error"));

            // When & Then
            assertThrows(ReportingFailedException.class,
                    () -> testomatApiClient.reportTest(TEST_RUN_UID, testRunResult1));
        }
    }

    @Test
    void reportTests_WithValidResults_ShouldCallHttpClient() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        List<TestRunResult> results = Arrays.asList(testRunResult1, testRunResult2);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildReportTestUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildBatchTestReportBody(results, API_KEY)).thenReturn(REQUEST_BODY);

            // When
            testomatApiClient.reportTests(TEST_RUN_UID, results);

            // Then
            verify(requestBodyBuilder).buildBatchTestReportBody(results, API_KEY);
            verify(httpClient).post(TEST_URL, REQUEST_BODY, null);
        }
    }

    @Test
    void reportTests_WithNullResults_ShouldReturnEarly() throws IOException {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        // When
        testomatApiClient.reportTests(TEST_RUN_UID, null);

        // Then
        verify(httpClient, never()).post(anyString(), anyString(), any());
    }

    @Test
    void reportTests_WithEmptyResults_ShouldReturnEarly() throws IOException {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        // When
        testomatApiClient.reportTests(TEST_RUN_UID, Collections.emptyList());

        // Then
        verify(httpClient, never()).post(anyString(), anyString(), any());
    }

    @Test
    void reportTests_WithHttpException_ShouldThrowReportingFailedException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        List<TestRunResult> results = Arrays.asList(testRunResult1);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildReportTestUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildBatchTestReportBody(results, API_KEY)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, null))
                    .thenThrow(new IOException("Network error"));

            // When & Then
            assertThrows(ReportingFailedException.class,
                    () -> testomatApiClient.reportTests(TEST_RUN_UID, results));
        }
    }

    @Test
    void finishTestRun_WithValidParameters_ShouldCallHttpClient() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        float duration = 123.45f;

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildFinishTestRunUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildFinishRunBody(duration)).thenReturn(REQUEST_BODY);

            // When
            testomatApiClient.finishTestRun(TEST_RUN_UID, duration);

            // Then
            verify(requestBodyBuilder).buildFinishRunBody(duration);
            verify(httpClient).put(TEST_URL, REQUEST_BODY, null);
        }
    }

    @Test
    void finishTestRun_WithHttpException_ShouldThrowFinishReportFailedException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        float duration = 123.45f;

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildFinishTestRunUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildFinishRunBody(duration)).thenReturn(REQUEST_BODY);
            when(httpClient.put(TEST_URL, REQUEST_BODY, null))
                    .thenThrow(new IOException("Network error"));

            // When & Then
            assertThrows(FinishReportFailedException.class,
                    () -> testomatApiClient.finishTestRun(TEST_RUN_UID, duration));
        }
    }

    @Test
    void finishTestRun_WithZeroDuration_ShouldWork() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        float duration = 0.0f;

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildFinishTestRunUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildFinishRunBody(duration)).thenReturn(REQUEST_BODY);

            // When
            testomatApiClient.finishTestRun(TEST_RUN_UID, duration);

            // Then
            verify(requestBodyBuilder).buildFinishRunBody(duration);
            verify(httpClient).put(TEST_URL, REQUEST_BODY, null);
        }
    }

    @Test
    void finishTestRun_WithNegativeDuration_ShouldWork() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        float duration = -1.0f;

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildFinishTestRunUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildFinishRunBody(duration)).thenReturn(REQUEST_BODY);

            // When
            testomatApiClient.finishTestRun(TEST_RUN_UID, duration);

            // Then
            verify(requestBodyBuilder).buildFinishRunBody(duration);
            verify(httpClient).put(TEST_URL, REQUEST_BODY, null);
        }
    }

    @Test
    void reportTest_WithNullResult_ShouldThrowReportingFailedException() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(() -> RequestUrlBuilderUtil.buildReportTestUrl(TEST_RUN_UID))
                    .thenReturn(TEST_URL);
            when(requestBodyBuilder.buildSingleTestReportBody(null))
                    .thenThrow(new NullPointerException("Null result"));

            // When & Then
            assertThrows(ReportingFailedException.class,
                    () -> testomatApiClient.reportTest(TEST_RUN_UID, null));
        }
    }

    @Test
    void createRun_WithNullTitle_ShouldPassToBuilder() throws Exception {
        // Given
        testomatApiClient = new TestomatApiClient(API_KEY, httpClient, requestBodyBuilder);

        Map<String, String> response = new HashMap<>();
        response.put(RESPONSE_UID_KEY, TEST_RUN_UID);

        try (MockedStatic<RequestUrlBuilderUtil> urlBuilderMock = mockStatic(RequestUrlBuilderUtil.class)) {
            urlBuilderMock.when(RequestUrlBuilderUtil::buildCreateRunUrl).thenReturn(TEST_URL);
            when(requestBodyBuilder.buildCreateRunBody(null)).thenReturn(REQUEST_BODY);
            when(httpClient.post(TEST_URL, REQUEST_BODY, Map.class)).thenReturn(response);

            // When
            String result = testomatApiClient.createRun(null);

            // Then
            assertEquals(TEST_RUN_UID, result);
            verify(requestBodyBuilder).buildCreateRunBody(null);
        }
    }
}