package io.testomat.cucumber.extractor;

import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStep;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.cucumber.exception.StatusNormalizerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.testomat.core.constants.CommonConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestDataExtractorTest {

    @Mock
    private TestCaseFinished testCaseFinished;

    @Mock
    private TestCase testCase;

    @Mock
    private Result result;

    @Mock
    private PickleStepTestStep pickleStepTestStep;

    @Mock
    private TestStep testStep;

    private TestDataExtractor extractor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        extractor = new TestDataExtractor();
    }

    @Test
    void shouldCreateExampleFromStepText() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTestSteps()).thenReturn(Arrays.asList(pickleStepTestStep, testStep));
        when(pickleStepTestStep.getStepText()).thenReturn("I enter 'John' and age 25 and amount 10.5");

        // When
        Map<Object, Object> example = extractor.createExample(testCaseFinished);

        // Then
        assertNotNull(example);
        assertEquals("John", example.get("step_value_0"));
        assertEquals(25L, example.get("step_value_1"));
        assertEquals(10.5, example.get("step_value_2"));
    }

    @Test
    void shouldCreateEmptyExampleWhenNoStepText() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(testStep));

        // When
        Map<Object, Object> example = extractor.createExample(testCaseFinished);

        // Then
        assertNotNull(example);
        assertTrue(example.isEmpty());
    }

    @Test
    void shouldExtractValuesFromComplexStepText() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(pickleStepTestStep));
        when(pickleStepTestStep.getStepText()).thenReturn("I buy \"apple\" for 1.99 and \"banana\" for 2 items");

        // When
        Map<Object, Object> example = extractor.createExample(testCaseFinished);

        // Then
        assertEquals("apple", example.get("step_value_0"));
        assertEquals(1.99, example.get("step_value_1"));
        assertEquals("banana", example.get("step_value_2"));
        assertEquals(2L, example.get("step_value_3"));
    }

    @Test
    void shouldGenerateRidWithSimpleValues() {
        // Given
        URI uri = URI.create("classpath:features/login.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Login");
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(pickleStepTestStep));
        when(pickleStepTestStep.getStepText()).thenReturn("I enter 'John' and age 25 and amount 10.5");

        // When
        String rid = extractor.generateRid(testCaseFinished);

        // Then
        assertEquals("classpath:features/login.feature.Login-step_value_0_John"
                + "-step_value_1_25-step_value_2_10.5", rid);
    }

    @Test
    void shouldGenerateRidWithHashForComplexValue() {
        // Given
        URI uri = URI.create("classpath:features/orders.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Place order");
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(pickleStepTestStep));
        when(pickleStepTestStep.getStepText()).thenReturn("I place order with 'long complex order reference #123 !'");

        // When
        String rid = extractor.generateRid(testCaseFinished);

        // Then
        assertTrue(rid.startsWith("classpath:features/orders.feature.Place order-step_value_0_h"));
    }

    @Test
    void shouldGenerateRidWithoutParameters() {
        // Given
        URI uri = URI.create("classpath:features/smoke.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Health check");
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(testStep));

        // When
        String rid = extractor.generateRid(testCaseFinished);

        // Then
        assertEquals("classpath:features/smoke.feature.Health check", rid);
    }

    @Test
    void shouldExtractExceptionDetailsWhenErrorExists() {
        // Given
        RuntimeException testException = new RuntimeException("Test error message");
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getError()).thenReturn(testException);

        // When
        ExceptionDetails details = extractor.extractExceptionDetails(testCaseFinished);

        // Then
        assertNotNull(details);
        assertEquals("Test error message", details.getMessage());
        assertNotNull(details.getStack());
        assertTrue(details.getStack().contains("RuntimeException"));
        assertTrue(details.getStack().contains("Test error message"));
    }

    @Test
    void shouldReturnEmptyExceptionDetailsWhenNoError() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getError()).thenReturn(null);

        // When
        ExceptionDetails details = extractor.extractExceptionDetails(testCaseFinished);

        // Then
        assertNotNull(details);
        assertNull(details.getMessage());
        assertNull(details.getStack());
    }

    @Test
    void shouldExtractTestIdFromTags() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList("@smoke", "@T12345678", "@regression"));

        // When
        String testId = extractor.extractTestId(testCaseFinished);

        // Then
        assertEquals("@T12345678", testId);
    }

    @Test
    void shouldReturnNullWhenNoValidTestId() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList("@smoke", "@regression", "@invalid"));

        // When
        String testId = extractor.extractTestId(testCaseFinished);

        // Then
        assertNull(testId);
    }

    @Test
    void shouldReturnNullWhenTestCaseIsNull() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(null);

        // When
        String testId = extractor.extractTestId(testCaseFinished);

        // Then
        assertNull(testId);
    }

    @Test
    void shouldReturnNullWhenTagsAreNull() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(null);

        // When
        String testId = extractor.extractTestId(testCaseFinished);

        // Then
        assertNull(testId);
    }

    @Test
    void shouldExtractTitleFromTags() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList("@smoke", "@title:User_Login_Test", "@regression"));

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("User Login Test", title);
    }

    @Test
    void shouldReturnTestNameWhenNoTitleTag() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList("@smoke", "@regression"));
        when(testCase.getName()).thenReturn("Test Scenario Name");

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Test Scenario Name", title);
    }

    @Test
    void shouldReturnUnknownTestWhenTestCaseIsNull() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(null);

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Unknown test", title);
    }

    @Test
    void shouldReturnUnknownTestWhenTestNameIsNull() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Collections.emptyList());
        when(testCase.getName()).thenReturn(null);

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Unknown test", title);
    }

    @Test
    void shouldExtractFileNameFromUri() {
        // Given
        URI testUri = URI.create("file:///path/to/test/TestFeature.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertEquals("file:///path/to/test/TestFeature.feature", fileName);
    }

    @Test
    void shouldExtractFileNameFromWindowsPath() {
        // Given
        URI testUri = URI.create("file:///C:/path/to/test/TestFeature.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertEquals("file:///C:/path/to/test/TestFeature.feature", fileName);
    }

    @Test
    void shouldHandleColonInPath() {
        // Given
        URI testUri = URI.create("file://C:/Users/test/features/TestFeature.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertEquals("file://C:/Users/test/features/TestFeature.feature", fileName);
    }

    @Test
    void shouldReturnNullWhenFileExtractionFails() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenThrow(new RuntimeException("URI error"));

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertNull(fileName);
    }

    @Test
    void shouldNormalizePassedStatus() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.PASSED);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(PASSED, normalizedStatus);
    }

    @Test
    void shouldNormalizeFailedStatus() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.FAILED);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(FAILED, normalizedStatus);
    }

    @Test
    void shouldNormalizeSkippedStatus() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.SKIPPED);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(SKIPPED, normalizedStatus);
    }

    @Test
    void shouldNormalizePendingStatusToSkipped() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.PENDING);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(SKIPPED, normalizedStatus);
    }

    @Test
    void shouldNormalizeUndefinedStatusToSkipped() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.UNDEFINED);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(SKIPPED, normalizedStatus);
    }

    @Test
    void shouldNormalizeAmbiguousStatusToSkipped() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(result);
        when(result.getStatus()).thenReturn(Status.AMBIGUOUS);

        // When
        String normalizedStatus = extractor.getNormalizedStatus(testCaseFinished);

        // Then
        assertEquals(SKIPPED, normalizedStatus);
    }

    @Test
    void shouldThrowExceptionWhenResultIsNull() {
        // Given
        when(testCaseFinished.getResult()).thenReturn(null);

        // When & Then
        StatusNormalizerException exception = assertThrows(
                StatusNormalizerException.class,
                () -> extractor.getNormalizedStatus(testCaseFinished)
        );
        
        assertTrue(exception.getMessage().contains("Result is null from event"));
    }

    @Test
    void shouldHandleNullTagsInExtractTestId() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList(null, "@T12345678", null));

        // When
        String testId = extractor.extractTestId(testCaseFinished);

        // Then
        assertEquals("@T12345678", testId);
    }

    @Test
    void shouldHandleNullTagsInExtractTitle() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList(null, "@title:Test_Title", null));

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Test Title", title);
    }

    @Test
    void shouldHandleCaseInsensitiveTitleTags() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Arrays.asList("@Title:Case_Test", "@TITLE:Another_Test"));

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Case Test", title);
    }

    @Test
    void shouldReturnUnknownTestWhenTestCaseThrowsException() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTags()).thenReturn(Collections.emptyList());
        when(testCase.getName()).thenThrow(new RuntimeException("Test error"));

        // When
        String title = extractor.extractTitle(testCaseFinished);

        // Then
        assertEquals("Unknown test", title);
    }

    @Test
    void shouldExtractFileNameReturnsUriToString() {
        // Given
        URI testUri = URI.create("classpath:features/TestFeature.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertEquals("classpath:features/TestFeature.feature", fileName);
    }

    @Test
    void shouldHandleHttpUriInExtractFileName() {
        // Given
        URI testUri = URI.create("http://example.com/test.feature");
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getUri()).thenReturn(testUri);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertEquals("http://example.com/test.feature", fileName);
    }

    @Test
    void shouldHandleNullTestCaseInExtractFileName() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(null);

        // When
        String fileName = extractor.extractFileName(testCaseFinished);

        // Then
        assertNull(fileName);
    }

    @Test
    void shouldParseMultipleTypesFromSingleStep() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(pickleStepTestStep));
        when(pickleStepTestStep.getStepText()).thenReturn("User enters \"test@example.com\" and password \"secret123\" and age 25 and amount 99.99");

        // When
        Map<Object, Object> example = extractor.createExample(testCaseFinished);

        // Then
        assertEquals("test@example.com", example.get("step_value_0"));
        assertEquals("secret123", example.get("step_value_1"));
        assertEquals(25L, example.get("step_value_2"));
        assertEquals(99.99, example.get("step_value_3"));
    }

    @Test
    void shouldCreateExampleWithBooleanValues() {
        // Given
        when(testCaseFinished.getTestCase()).thenReturn(testCase);
        when(testCase.getTestSteps()).thenReturn(Collections.singletonList(pickleStepTestStep));
        when(pickleStepTestStep.getStepText()).thenReturn("Set active to 'true' and visible to 'false'");

        // When
        Map<Object, Object> example = extractor.createExample(testCaseFinished);

        // Then
        assertEquals("true", example.get("step_value_0"));
        assertEquals("false", example.get("step_value_1"));
    }
}