package com.testomatio.reporter.core.extractor;

import com.testomatio.reporter.annotation.TestId;
import com.testomatio.reporter.annotation.Title;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.event.TestCase;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;

class TestMetadataExtractorTest {

    private TestMetadataExtractor extractor;
    private AutoCloseable mockitoCloseable;
    private MockitoSession mockitoSession;

    @Mock
    private ExtensionContext extensionContext;

    @Mock
    private ITestResult testResult;

    @Mock
    private ITestNGMethod testNGMethod;

    @Mock
    private ITestClass testClass;

    @Mock
    private ConstructorOrMethod constructorOrMethod;

    @Mock
    private TestCase testCase;

    @BeforeAll
    static void setUpClass() {
        // Агресивна очистка всього стану Mockito
        try {
            org.mockito.Mockito.framework().clearInlineMocks();
            org.mockito.Mockito.validateMockitoUsage();
        } catch (Exception e) {
            // Ігноруємо помилки при очистці
        }
    }

    @AfterAll
    static void tearDownClass() {
        try {
            org.mockito.Mockito.framework().clearInlineMocks();
            org.mockito.Mockito.validateMockitoUsage();
        } catch (Exception e) {
            // Ігноруємо помилки при очистці
        }
    }

    @BeforeEach
    void setUp() {
        // Спочатку спробуємо очистити будь-які незавершені stubbing'и
        try {
            org.mockito.Mockito.validateMockitoUsage();
        } catch (Exception e) {
            // Ігноруємо помилки валідації з попередніх тестів
        }

        // Створюємо ізольовану Mockito сесію
        mockitoSession = mockitoSession()
                .strictness(Strictness.LENIENT)  // Більш толерантний режим
                .startMocking();

        // Ініціалізуємо моки
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        extractor = new TestMetadataExtractor();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Закриваємо ресурси в зворотному порядку
        if (mockitoCloseable != null) {
            try {
                mockitoCloseable.close();
            } catch (Exception e) {
                // Ігноруємо помилки закриття
            }
        }

        if (mockitoSession != null) {
            try {
                mockitoSession.finishMocking();
            } catch (Exception e) {
                // Ігноруємо помилки завершення сесії
            }
        }

        // Фінальна очистка
        try {
            org.mockito.Mockito.validateMockitoUsage();
        } catch (Exception e) {
            // Ігноруємо помилки валідації
        }
    }

    // Окремий клас для тестових методів - повністю ізольований
    public static class DummyTestClass {
        @Title("Test Method with Title")
        @TestId("@T12345678")
        public void methodWithAnnotations() {
        }

        public void methodWithoutAnnotations() {
        }

        @Title("Disabled Test Title")
        @TestId("@T87654321")
        public void disabledMethod() {
        }
    }

    @Test
    void extractFromJUnit_WithTitleAnnotation_ShouldExtractTitle() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("methodWithAnnotations");

        when(extensionContext.getDisplayName()).thenReturn("Display Name");
        when(extensionContext.getTestClass()).thenReturn(Optional.of(DummyTestClass.class));

        // When
        TestMetadata result = extractor.extractFromJUnit(method, extensionContext);

        // Then
        assertEquals("Test Method with Title", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("DummyTestClass", result.getSuiteTitle());
        assertEquals("DummyTestClass.java", result.getFile());
    }

    @Test
    void extractFromJUnit_WithoutTitleAnnotation_ShouldUseDisplayName() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("methodWithoutAnnotations");

        when(extensionContext.getDisplayName()).thenReturn("Custom Display Name");
        when(extensionContext.getTestClass()).thenReturn(Optional.of(DummyTestClass.class));

        // When
        TestMetadata result = extractor.extractFromJUnit(method, extensionContext);

        // Then
        assertEquals("Custom Display Name", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("DummyTestClass", result.getSuiteTitle());
        assertEquals("DummyTestClass.java", result.getFile());
    }

    @Test
    void extractFromJUnit_WithoutTestClass_ShouldUseDefaultSuite() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("methodWithoutAnnotations");

        when(extensionContext.getDisplayName()).thenReturn("Display Name");
        when(extensionContext.getTestClass()).thenReturn(Optional.empty());

        // When
        TestMetadata result = extractor.extractFromJUnit(method, extensionContext);

        // Then
        assertEquals("Display Name", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("Unknown", result.getSuiteTitle());
        assertEquals("Unknown.java", result.getFile());
    }

    @Test
    void extractFromTestNG_WithAnnotations_ShouldExtractMetadata() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("methodWithAnnotations");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testClass.getName()).thenReturn("com.example.TestClass");
        when(testResult.getName()).thenReturn("testResultName");

        // When
        TestMetadata result = extractor.extractFromTestNG(testResult);

        // Then
        assertEquals("Test Method with Title", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("com.example.TestClass", result.getSuiteTitle());
        assertEquals("com.example.TestClass.java", result.getFile());
    }

    @Test
    void extractFromTestNG_WithoutTitleAnnotation_ShouldUseResultName() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("methodWithoutAnnotations");

        when(testResult.getMethod()).thenReturn(testNGMethod);
        when(testNGMethod.getConstructorOrMethod()).thenReturn(constructorOrMethod);
        when(constructorOrMethod.getMethod()).thenReturn(method);
        when(testResult.getName()).thenReturn("TestNG Result Name");
        when(testResult.getTestClass()).thenReturn(testClass);
        when(testClass.getName()).thenReturn("com.example.TestClass");

        // When
        TestMetadata result = extractor.extractFromTestNG(testResult);

        // Then
        assertEquals("TestNG Result Name", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("com.example.TestClass", result.getSuiteTitle());
        assertEquals("com.example.TestClass.java", result.getFile());
    }

    @Test
    void extractFromTestNGDisabled_ShouldExtractMetadata() throws NoSuchMethodException {
        // Given
        Method method = DummyTestClass.class.getMethod("disabledMethod");
        Class<?> testClass = DummyTestClass.class;

        // When
        TestMetadata result = extractor.extractFromTestNGDisabled(method, testClass);

        // Then
        assertEquals("Disabled Test Title", result.getTitle());
        assertEquals("@T87654321", result.getTestId());
        assertEquals("DummyTestClass", result.getSuiteTitle());
        assertEquals("DummyTestClass.java", result.getFile());
    }

    @Test
    void extractFromCucumber_WithValidTags_ShouldExtractMetadata() {
        // Given
        List<String> tags = Arrays.asList("@T12345678", "@title:Cucumber_Test_Title", "@smoke");
        URI uri = URI.create("classpath:features/example.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Cucumber Test Title", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("example", result.getSuiteTitle());
        assertEquals("example.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithoutTitleTag_ShouldUseScenarioName() {
        // Given
        List<String> tags = Arrays.asList("@T12345678", "@smoke");
        URI uri = URI.create("file:src/test/resources/features/test.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Test Scenario");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Test Scenario", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("test", result.getSuiteTitle());
        assertEquals("test.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithoutTestId_ShouldReturnNullTestId() {
        // Given
        List<String> tags = Arrays.asList("@title:Test_Title", "@smoke");
        URI uri = URI.create("features/example.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Test Title", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("example", result.getSuiteTitle());
        assertEquals("example.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithNullTestCase_ShouldReturnUnknownValues() {
        // When
        TestMetadata result = extractor.extractFromCucumber(null);

        // Then
        assertEquals("Unknown Test", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("Unknown Suite", result.getSuiteTitle());
        assertEquals("unknown.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithNullTags_ShouldUseScenarioName() {
        // Given
        URI uri = URI.create("features/example.feature");

        when(testCase.getTags()).thenReturn(null);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Scenario Name", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("example", result.getSuiteTitle());
        assertEquals("example.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithEmptyTags_ShouldUseScenarioName() {
        // Given
        List<String> tags = Collections.emptyList();
        URI uri = URI.create("features/example.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Scenario Name", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("example", result.getSuiteTitle());
        assertEquals("example.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithNullUri_ShouldUseUnknownValues() {
        // Given
        List<String> tags = Arrays.asList("@T12345678");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(null);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Scenario Name", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("Unknown Suite", result.getSuiteTitle());
        assertEquals("unknown.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithComplexUri_ShouldParseCorrectly() {
        // Given
        List<String> tags = Arrays.asList("@T12345678");
        URI uri = URI.create("jar:file:/path/to/jar!/features/nested/complex.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Complex Scenario");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Complex Scenario", result.getTitle());
        assertEquals("@T12345678", result.getTestId());
        assertEquals("complex", result.getSuiteTitle());
        assertEquals("complex.feature", result.getFile());
    }

    @Test
    void extractFromCucumber_WithInvalidTestIdFormat_ShouldReturnNull() {
        // Given
        List<String> tags = Arrays.asList("@INVALID123", "@title:Test_Title", "@smoke");
        URI uri = URI.create("features/example.feature");

        when(testCase.getTags()).thenReturn(tags);
        when(testCase.getUri()).thenReturn(uri);
        when(testCase.getName()).thenReturn("Scenario Name");

        // When
        TestMetadata result = extractor.extractFromCucumber(testCase);

        // Then
        assertEquals("Test Title", result.getTitle());
        assertNull(result.getTestId());
        assertEquals("example", result.getSuiteTitle());
        assertEquals("example.feature", result.getFile());
    }
}