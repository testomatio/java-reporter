package io.testomat.core.client.request;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.testomat.core.constants.ApiRequestFields;
import io.testomat.core.constants.PropertyNameConstants;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NativeRequestBodyBuilder Tests")
class NativeRequestBodyBuilderTest {

    private NativeRequestBodyBuilder requestBodyBuilder;
    private PropertyProvider mockPropertyProvider;
    private PropertyProviderFactory mockFactory;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockPropertyProvider = mock(PropertyProvider.class);
        mockFactory = mock(PropertyProviderFactory.class);

        when(mockFactory.getPropertyProvider()).thenReturn(mockPropertyProvider);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
        }
    }

    @Test
    @DisplayName("Should build basic create run body with title only")
    void buildCreateRunBody_BasicTitle_ShouldContainTitleField() throws Exception {
        String title = "Test Run Title";

        String result = requestBodyBuilder.buildCreateRunBody(title);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);
        assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
    }

    @Test
    @DisplayName("Should build create run body with environment property")
    void buildCreateRunBody_WithEnvironment_ShouldIncludeEnvironment() throws Exception {
        String title = "Test Run";
        String environment = "staging";

        when(mockPropertyProvider.getProperty(PropertyNameConstants.ENVIRONMENT_PROPERTY_NAME))
                .thenReturn(environment);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
            assertEquals(environment, jsonNode.get(ApiRequestFields.ENVIRONMENT).asText());
        }
    }

    @Test
    @DisplayName("Should build create run body with group title")
    void buildCreateRunBody_WithGroupTitle_ShouldIncludeGroupTitle() throws Exception {
        String title = "Test Run";
        String groupTitle = "Regression Tests";

        when(mockPropertyProvider.getProperty(PropertyNameConstants.RUN_GROUP_PROPERTY_NAME))
                .thenReturn(groupTitle);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
            assertEquals(groupTitle, jsonNode.get(ApiRequestFields.GROUP_TITLE).asText());
        }
    }

    @Test
    @DisplayName("Should build create run body with shared run property")
    void buildCreateRunBody_WithSharedRun_ShouldIncludeSharedRun() throws Exception {
        String title = "Test Run";
        String sharedRun = "true";

        when(mockPropertyProvider.getProperty(PropertyNameConstants.SHARED_RUN_PROPERTY_NAME))
                .thenReturn(sharedRun);

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
            assertEquals(sharedRun, jsonNode.get("shared_run").asText());
        }
    }

    @Test
    @DisplayName("Should build create run body with publish parameter")
    void buildCreateRunBody_WithPublishParam_ShouldIncludeAccessEvent() throws Exception {
        String title = "Test Run";

        when(mockPropertyProvider.getProperty(PropertyNameConstants.PUBLISH_PROPERTY_NAME))
                .thenReturn("true");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
            assertEquals("publish", jsonNode.get("access_event").asText());
        }
    }

    @Test
    @DisplayName("Should handle null properties gracefully")
    void buildCreateRunBody_NullProperties_ShouldOnlyIncludeTitle() throws Exception {
        String title = "Test Run";

        when(mockPropertyProvider.getProperty(anyString())).thenThrow(new RuntimeException("Property not found"));

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();
            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
            assertNull(jsonNode.get(ApiRequestFields.ENVIRONMENT));
            assertNull(jsonNode.get(ApiRequestFields.GROUP_TITLE));
            assertNull(jsonNode.get("shared_run"));
            assertNull(jsonNode.get("access_event"));
        }
    }

    @Test
    @DisplayName("Should build single test report body with all fields")
    void buildSingleTestReportBody_AllFields_ShouldIncludeAllData() throws Exception {
        TestResult testResult = new TestResult.Builder()
                .withTitle("Test Method Name")
                .withTestId("test-123")
                .withSuiteTitle("Test Suite")
                .withFile("TestClass.java")
                .withStatus("passed")
                .withMessage("Test passed successfully")
                .withStack("stack trace here")
                .withExample("example data")
                .withRid("rid-456")
                .build();

        String result = requestBodyBuilder.buildSingleTestReportBody(testResult);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("Test Method Name", jsonNode.get(ApiRequestFields.TITLE).asText());
        assertEquals("test-123", jsonNode.get(ApiRequestFields.TEST_ID).asText());
        assertEquals("Test Suite", jsonNode.get(ApiRequestFields.SUITE_TITLE).asText());
        assertEquals("TestClass.java", jsonNode.get(ApiRequestFields.FILE).asText());
        assertEquals("passed", jsonNode.get(ApiRequestFields.STATUS).asText());
        assertEquals("Test passed successfully", jsonNode.get(ApiRequestFields.MESSAGE).asText());
        assertEquals("stack trace here", jsonNode.get(ApiRequestFields.STACK).asText());
        assertEquals("example data", jsonNode.get("example").asText());
        assertEquals("rid-456", jsonNode.get("rid").asText());
    }

    @Test
    @DisplayName("Should build single test report body with minimal required fields")
    void buildSingleTestReportBody_MinimalFields_ShouldIncludeRequiredFieldsOnly() throws Exception {
        TestResult testResult = new TestResult.Builder()
                .withTitle("Test Method")
                .withSuiteTitle("Test Suite")
                .withFile("TestClass.java")
                .withStatus("failed")
                .build();

        String result = requestBodyBuilder.buildSingleTestReportBody(testResult);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("Test Method", jsonNode.get(ApiRequestFields.TITLE).asText());
        assertNull(jsonNode.get(ApiRequestFields.TEST_ID));
        assertEquals("Test Suite", jsonNode.get(ApiRequestFields.SUITE_TITLE).asText());
        assertEquals("TestClass.java", jsonNode.get(ApiRequestFields.FILE).asText());
        assertEquals("failed", jsonNode.get(ApiRequestFields.STATUS).asText());
        assertNull(jsonNode.get(ApiRequestFields.MESSAGE));
        assertNull(jsonNode.get(ApiRequestFields.STACK));
        assertNull(jsonNode.get("example"));
        assertNull(jsonNode.get("rid"));
    }

    @Test
    @DisplayName("Should include create parameter when configured")
    void buildSingleTestReportBody_WithCreateParam_ShouldIncludeCreateField() throws Exception {
        when(mockPropertyProvider.getProperty(PropertyNameConstants.CREATE_TEST_PROPERTY_NAME))
                .thenReturn("true");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
                     mockStatic(PropertyProviderFactoryImpl.class)) {
            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                    .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();

            TestResult testResult = new TestResult.Builder()
                    .withTitle("Test Method")
                    .withSuiteTitle("Test Suite")
                    .withFile("TestClass.java")
                    .withStatus("passed")
                    .build();

            String result = requestBodyBuilder.buildSingleTestReportBody(testResult);

            JsonNode jsonNode = objectMapper.readTree(result);
            assertEquals("true", jsonNode.get("create").asText());
        }
    }

    @Test
    @DisplayName("Should build batch test report body with multiple results")
    void buildBatchTestReportBody_MultipleResults_ShouldIncludeAllResults() throws Exception {
        TestResult result1 = new TestResult.Builder()
                .withTitle("Test 1")
                .withTestId("test-1")
                .withSuiteTitle("Suite 1")
                .withFile("Test1.java")
                .withStatus("passed")
                .build();

        TestResult result2 = new TestResult.Builder()
                .withTitle("Test 2")
                .withTestId("test-2")
                .withSuiteTitle("Suite 2")
                .withFile("Test2.java")
                .withStatus("failed")
                .withMessage("Assertion failed")
                .build();

        List<TestResult> results = Arrays.asList(result1, result2);
        String apiKey = "test-api-key";

        String result = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals(apiKey, jsonNode.get("api_key").asText());
        assertTrue(jsonNode.has("tests"));
        assertTrue(jsonNode.get("tests").isArray());
        assertEquals(2, jsonNode.get("tests").size());

        JsonNode firstTest = jsonNode.get("tests").get(0);
        assertEquals("Test 1", firstTest.get(ApiRequestFields.TITLE).asText());
        assertEquals("test-1", firstTest.get(ApiRequestFields.TEST_ID).asText());
        assertEquals("passed", firstTest.get(ApiRequestFields.STATUS).asText());

        JsonNode secondTest = jsonNode.get("tests").get(1);
        assertEquals("Test 2", secondTest.get(ApiRequestFields.TITLE).asText());
        assertEquals("failed", secondTest.get(ApiRequestFields.STATUS).asText());
        assertEquals("Assertion failed", secondTest.get(ApiRequestFields.MESSAGE).asText());
    }

    @Test
    @DisplayName("Should build batch test report body with empty results list")
    void buildBatchTestReportBody_EmptyResults_ShouldReturnValidJson() throws Exception {
        List<TestResult> results = Arrays.asList();
        String apiKey = "test-api-key";

        String result = requestBodyBuilder.buildBatchTestReportBody(results, apiKey);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals(apiKey, jsonNode.get("api_key").asText());
        assertTrue(jsonNode.has("tests"));
        assertTrue(jsonNode.get("tests").isArray());
        assertEquals(0, jsonNode.get("tests").size());
    }

    @Test
    @DisplayName("Should build finish run body with duration")
    void buildFinishRunBody_WithDuration_ShouldIncludeStatusAndDuration() throws Exception {
        float duration = 45.5f;

        String result = requestBodyBuilder.buildFinishRunBody(duration);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("finish", jsonNode.get(ApiRequestFields.STATUS_EVENT).asText());
        assertEquals(duration, jsonNode.get(ApiRequestFields.DURATION).floatValue(), 0.001);
    }

    @Test
    @DisplayName("Should handle zero duration")
    void buildFinishRunBody_ZeroDuration_ShouldIncludeZeroDuration() throws Exception {
        float duration = 0.0f;

        String result = requestBodyBuilder.buildFinishRunBody(duration);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("finish", jsonNode.get(ApiRequestFields.STATUS_EVENT).asText());
        assertEquals(0.0f, jsonNode.get(ApiRequestFields.DURATION).floatValue(), 0.001);
    }

    @Test
    @DisplayName("Should handle very large duration")
    void buildFinishRunBody_LargeDuration_ShouldIncludeLargeDuration() throws Exception {
        float duration = 999999.99f;

        String result = requestBodyBuilder.buildFinishRunBody(duration);

        assertNotNull(result);
        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("finish", jsonNode.get(ApiRequestFields.STATUS_EVENT).asText());
        assertEquals(duration, jsonNode.get(ApiRequestFields.DURATION).floatValue(), 0.01);
    }

    @Test
    @DisplayName("Should produce valid JSON for all methods")
    void allMethods_ShouldProduceValidJson() throws Exception {
        // Test create run body
        String createRunResult = requestBodyBuilder.buildCreateRunBody("Test Run");
        assertDoesNotThrow(() -> objectMapper.readTree(createRunResult));

        // Test single test report body
        TestResult testResult = new TestResult.Builder()
                .withTitle("Test")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("passed")
                .build();
        String singleTestResult = requestBodyBuilder.buildSingleTestReportBody(testResult);
        assertDoesNotThrow(() -> objectMapper.readTree(singleTestResult));

        // Test batch test report body
        String batchResult = requestBodyBuilder.buildBatchTestReportBody(
                Arrays.asList(testResult), "api-key");
        assertDoesNotThrow(() -> objectMapper.readTree(batchResult));

        // Test finish run body
        String finishResult = requestBodyBuilder.buildFinishRunBody(30.0f);
        assertDoesNotThrow(() -> objectMapper.readTree(finishResult));
    }

    @Test
    @DisplayName("Should handle special characters in strings")
    void buildMethods_WithSpecialCharacters_ShouldEscapeProperlyInJson() throws Exception {
        String specialTitle = "Test with \"quotes\" and \n newlines \t tabs";

        // Test create run with special characters
        String createResult = requestBodyBuilder.buildCreateRunBody(specialTitle);
        JsonNode createNode = objectMapper.readTree(createResult);
        assertEquals(specialTitle, createNode.get(ApiRequestFields.TITLE).asText());

        // Test single test report with special characters
        TestResult testResult = new TestResult.Builder()
                .withTitle(specialTitle)
                .withSuiteTitle("Suite with special chars: @#$%")
                .withFile("Test.java")
                .withStatus("failed")
                .withMessage("Error: \"Something went wrong\" with special chars: <>")
                .build();

        String singleResult = requestBodyBuilder.buildSingleTestReportBody(testResult);
        JsonNode singleNode = objectMapper.readTree(singleResult);
        assertEquals(specialTitle, singleNode.get(ApiRequestFields.TITLE).asText());
        assertEquals("Suite with special chars: @#$%", singleNode.get(ApiRequestFields.SUITE_TITLE).asText());
        assertEquals("Error: \"Something went wrong\" with special chars: <>",
                singleNode.get(ApiRequestFields.MESSAGE).asText());
    }
}