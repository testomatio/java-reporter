package io.testomat.core.client.request;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import io.testomat.core.facade.methods.artifact.TempArtifactDirectoriesStorage;
import io.testomat.core.model.Link;
import io.testomat.core.model.TestResult;
import io.testomat.core.propertyconfig.impl.PropertyProviderFactoryImpl;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;
import io.testomat.core.step.StepData;
import io.testomat.core.step.TestStep;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

        TempArtifactDirectoriesStorage.STEP_DATA.clear();
        TempArtifactDirectoriesStorage.DIRECTORIES.remove();
    }

    @Test
    @DisplayName("Should build basic create run body with title only")
    void buildCreateRunBodyBasicTitleShouldContainTitleField() throws Exception {

        String title = "Test Run Title";

        String result = requestBodyBuilder.buildCreateRunBody(title);

        assertNotNull(result);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals(title, jsonNode.get(ApiRequestFields.TITLE).asText());
    }

    @Test
    @DisplayName("Should build create run body with environment property")
    void buildCreateRunBodyWithEnvironmentShouldIncludeEnvironment() throws Exception {

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
    void buildCreateRunBodyWithGroupTitleShouldIncludeGroupTitle() throws Exception {

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
    void buildCreateRunBodyWithSharedRunShouldIncludeSharedRun() throws Exception {

        String title = "Test Run";

        when(mockPropertyProvider.getProperty(PropertyNameConstants.SHARED_RUN_PROPERTY_NAME))
            .thenReturn("true");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
            mockStatic(PropertyProviderFactoryImpl.class)) {

            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();

            String result = requestBodyBuilder.buildCreateRunBody(title);

            JsonNode jsonNode = objectMapper.readTree(result);

            assertEquals("true", jsonNode.get("shared_run").asText());
        }
    }

    @Test
    @DisplayName("Should build create run body with publish parameter")
    void buildCreateRunBodyWithPublishParamShouldIncludeAccessEvent() throws Exception {

        when(mockPropertyProvider.getProperty(PropertyNameConstants.PUBLISH_PROPERTY_NAME))
            .thenReturn("true");

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
            mockStatic(PropertyProviderFactoryImpl.class)) {

            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();

            String result = requestBodyBuilder.buildCreateRunBody("Test Run");

            JsonNode jsonNode = objectMapper.readTree(result);

            assertEquals("publish", jsonNode.get("access_event").asText());
        }
    }

    @Test
    @DisplayName("Should include overwrite=true in create run body")
    void buildCreateRunBodyShouldIncludeOverwrite() throws Exception {

        String result = requestBodyBuilder.buildCreateRunBody("Run");

        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("true", jsonNode.get("overwrite").asText());
    }

    @Test
    @DisplayName("Should handle null properties gracefully")
    void buildCreateRunBodyNullPropertiesShouldOnlyIncludeTitle() throws Exception {

        when(mockPropertyProvider.getProperty(anyString()))
            .thenThrow(new RuntimeException("Property not found"));

        try (MockedStatic<PropertyProviderFactoryImpl> mockedStatic =
            mockStatic(PropertyProviderFactoryImpl.class)) {

            mockedStatic.when(PropertyProviderFactoryImpl::getPropertyProviderFactory)
                .thenReturn(mockFactory);

            requestBodyBuilder = new NativeRequestBodyBuilder();

            String result = requestBodyBuilder.buildCreateRunBody("Test Run");

            JsonNode jsonNode = objectMapper.readTree(result);

            assertNull(jsonNode.get(ApiRequestFields.ENVIRONMENT));
            assertNull(jsonNode.get(ApiRequestFields.GROUP_TITLE));
            assertNull(jsonNode.get("shared_run"));
            assertNull(jsonNode.get("access_event"));
        }
    }

    @Test
    @DisplayName("Should build single test report body with all fields")
    void buildSingleTestReportBodyAllFieldsShouldIncludeAllData() throws Exception {

        List<Link> links = Arrays.asList(
            Link.test("T-123"),
            Link.label("Smoke")
        );

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
            .withOverwrite(false)
            .withLinks(links)
            .build();

        String result = requestBodyBuilder.buildSingleTestReportBody(testResult);

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

        assertFalse(jsonNode.get("overwrite").asBoolean());

        assertTrue(jsonNode.has("links"));
        assertEquals(2, jsonNode.get("links").size());

        JsonNode firstLink = jsonNode.get("links").get(0);
        assertEquals("T-123", firstLink.get("test").asText());

        JsonNode secondLink = jsonNode.get("links").get(1);
        assertEquals("Smoke", secondLink.get("label").asText());
    }

    @Test
    @DisplayName("Should build single test report body with minimal required fields")
    void buildSingleTestReportBodyMinimalFieldsShouldIncludeRequiredFieldsOnly() throws Exception {

        TestResult testResult = new TestResult.Builder()
            .withTitle("Test Method")
            .withSuiteTitle("Test Suite")
            .withFile("TestClass.java")
            .withStatus("failed")
            .build();

        String result = requestBodyBuilder.buildSingleTestReportBody(testResult);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("Test Method", jsonNode.get(ApiRequestFields.TITLE).asText());
        assertEquals("failed", jsonNode.get(ApiRequestFields.STATUS).asText());

        assertTrue(jsonNode.get("overwrite").asBoolean());
        assertTrue(jsonNode.get("links") == null || jsonNode.get("links").isNull());
    }

    @Test
    @DisplayName("Should include create parameter when configured")
    void buildSingleTestReportBodyWithCreateParamShouldIncludeCreateField() throws Exception {

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
    void buildBatchTestReportBodyMultipleResultsShouldIncludeAllResults() throws Exception {

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

        String result = requestBodyBuilder.buildBatchTestReportBody(
            Arrays.asList(result1, result2),
            "test-api-key"
        );

        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("test-api-key", jsonNode.get("api_key").asText());
        assertEquals(2, jsonNode.get("tests").size());
    }

    @Test
    @DisplayName("Should build finish run body with duration")
    void buildFinishRunBodyWithDurationShouldIncludeStatusAndDuration() throws Exception {

        String result = requestBodyBuilder.buildFinishRunBody(45.5f);

        JsonNode jsonNode = objectMapper.readTree(result);

        assertEquals("finish", jsonNode.get(ApiRequestFields.STATUS_EVENT).asText());
        assertEquals(45.5f, jsonNode.get(ApiRequestFields.DURATION).floatValue(), 0.001);
    }

    @Test
    @DisplayName("Should produce valid JSON for all methods")
    void allMethodsShouldProduceValidJson() throws Exception {

        String createRunResult = requestBodyBuilder.buildCreateRunBody("Test Run");
        assertDoesNotThrow(() -> objectMapper.readTree(createRunResult));

        TestResult testResult = new TestResult.Builder()
            .withTitle("Test")
            .withSuiteTitle("Suite")
            .withFile("Test.java")
            .withStatus("passed")
            .build();

        String singleTestResult = requestBodyBuilder.buildSingleTestReportBody(testResult);
        assertDoesNotThrow(() -> objectMapper.readTree(singleTestResult));

        String batchResult = requestBodyBuilder.buildBatchTestReportBody(
            Arrays.asList(testResult),
            "api-key"
        );

        assertDoesNotThrow(() -> objectMapper.readTree(batchResult));

        String finishResult = requestBodyBuilder.buildFinishRunBody(30.0f);

        assertDoesNotThrow(() -> objectMapper.readTree(finishResult));
    }

    @Test
    @DisplayName("Should attach step artifacts from storage")
    void shouldAttachStepArtifactsFromStorage() throws Exception {
        UUID stepId = UUID.randomUUID();

        StepData stepData = new StepData();
        stepData.getArtifacts().add("https://artifact");

        TempArtifactDirectoriesStorage.STEP_DATA
            .computeIfAbsent(Thread.currentThread().getId(), k -> new ConcurrentHashMap<>())
            .put(stepId, stepData);

        TestStep step = new TestStep();
        step.setId(stepId);

        TestResult result = new TestResult.Builder()
            .withTitle("Test")
            .withSuiteTitle("Suite")
            .withFile("Test.java")
            .withStatus("passed")
            .withSteps(Collections.singletonList(step))
            .build();

        requestBodyBuilder.buildSingleTestReportBody(result);

        assertArrayEquals(
            new String[]{"https://artifact"},
            step.getArtifacts()
        );
    }

    @Test
    @DisplayName("Should convert step directories to jsonl")
    void shouldConvertDirectoriesToJsonl() throws Exception {
        UUID stepId = UUID.randomUUID();

        StepData stepData = new StepData();
        stepData.getDirectories().add("dir1");

        TempArtifactDirectoriesStorage.STEP_DATA
            .computeIfAbsent(Thread.currentThread().getId(), k -> new ConcurrentHashMap<>())
            .put(stepId, stepData);

        TestStep step = new TestStep();
        step.setId(stepId);
        step.setStepTitle("Step");

        TestResult result = new TestResult.Builder()
            .withTitle("Test")
            .withSuiteTitle("Suite")
            .withFile("Test.java")
            .withStatus("passed")
            .withRid("rid")
            .withSteps(Collections.singletonList(step))
            .build();

        requestBodyBuilder.buildSingleTestReportBody(result);

        assertTrue(
            TempArtifactDirectoriesStorage.STEP_DATA
                .get(Thread.currentThread().getId())
                .isEmpty()
        );
    }

    @Test
    @DisplayName("Should handle missing step data")
    void shouldHandleMissingStepData() {
        UUID stepId = UUID.randomUUID();

        TestStep step = new TestStep();
        step.setId(stepId);

        TestResult result = new TestResult.Builder()
            .withTitle("Test")
            .withSuiteTitle("Suite")
            .withFile("Test.java")
            .withStatus("passed")
            .withSteps(Collections.singletonList(step))
            .build();

        assertDoesNotThrow(() ->
            requestBodyBuilder.buildSingleTestReportBody(result)
        );
    }
}