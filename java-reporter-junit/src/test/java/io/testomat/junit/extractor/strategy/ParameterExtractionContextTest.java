package io.testomat.junit.extractor.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for ParameterExtractionContext focusing on edge cases,
 * null handling, and proper interaction with ExtensionContext.
 */
class ParameterExtractionContextTest {

    @Test
    void constructor_shouldInitializeWithValidExtensionContext() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("testMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(extensionContext.getDisplayName()).thenReturn("Test Display Name");
        when(extensionContext.getUniqueId()).thenReturn("unique-id-123");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getExtensionContext()).isEqualTo(extensionContext);
        assertThat(context.getTestMethod()).isEqualTo(testMethod);
        assertThat(context.getDisplayName()).isEqualTo("Test Display Name");
        assertThat(context.getUniqueId()).isEqualTo("unique-id-123");
        assertThat(context.isValid()).isTrue();
    }

    @Test
    void constructor_shouldHandleMissingTestMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("Display Name");
        when(extensionContext.getUniqueId()).thenReturn("unique-id");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getTestMethod()).isNull();
        assertThat(context.getAnnotations()).isEmpty();
        assertThat(context.isValid()).isFalse();
    }

    @Test
    void getAnnotations_shouldReturnEmptyArrayWhenNoTestMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getAnnotations()).isEmpty();
    }

    @Test
    void getAnnotations_shouldReturnCopyOfAnnotations() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("annotatedMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        Annotation[] annotations1 = context.getAnnotations();
        Annotation[] annotations2 = context.getAnnotations();

        // Should return different array instances (defensive copy)
        assertThat(annotations1).isNotSameAs(annotations2);
        assertThat(annotations1).hasSize(annotations2.length);
    }

    @Test
    void hasAnnotation_shouldReturnTrueWhenAnnotationPresent() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("annotatedMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.hasAnnotation(Test.class)).isTrue();
    }

    @Test
    void hasAnnotation_shouldReturnFalseWhenAnnotationNotPresent() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("testMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.hasAnnotation(Deprecated.class)).isFalse();
    }

    @Test
    void hasAnnotation_shouldReturnFalseWhenNoTestMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.hasAnnotation(Test.class)).isFalse();
    }

    @Test
    void getAnnotation_shouldReturnAnnotationWhenPresent() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("annotatedMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        Test annotation = context.getAnnotation(Test.class);

        assertThat(annotation).isNotNull();
    }

    @Test
    void getAnnotation_shouldReturnNullWhenAnnotationNotPresent() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("testMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        Deprecated annotation = context.getAnnotation(Deprecated.class);

        assertThat(annotation).isNull();
    }

    @Test
    void getAnnotation_shouldReturnNullWhenNoTestMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        Test annotation = context.getAnnotation(Test.class);

        assertThat(annotation).isNull();
    }

    @Test
    void isValid_shouldReturnTrueWhenBothContextAndMethodPresent() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("testMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.isValid()).isTrue();
    }

    @Test
    void isValid_shouldReturnFalseWhenTestMethodMissing() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.isValid()).isFalse();
    }

    @Test
    void getParameterName_shouldReturnNameFromResolver() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("methodWithParams", String.class, int.class);
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        SourceCodeParameterNameResolver mockResolver = mock(SourceCodeParameterNameResolver.class);
        when(mockResolver.getParameterName(testMethod, 0)).thenReturn("customParam0");
        when(mockResolver.getParameterName(testMethod, 1)).thenReturn("customParam1");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext, mockResolver);

        assertThat(context.getParameterName(0)).isEqualTo("customParam0");
        assertThat(context.getParameterName(1)).isEqualTo("customParam1");
    }

    @Test
    void getParameterName_shouldReturnFallbackWhenNoTestMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getParameterName(0)).isEqualTo("param0");
        assertThat(context.getParameterName(5)).isEqualTo("param5");
    }

    @Test
    void getParameterName_shouldHandleNegativeIndex() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        // Should still return fallback format
        assertThat(context.getParameterName(-1)).isEqualTo("param-1");
    }

    @Test
    void getDisplayName_shouldReturnCorrectDisplayName() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getDisplayName()).thenReturn("My Test [param1, param2]");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getDisplayName()).isEqualTo("My Test [param1, param2]");
    }

    @Test
    void getUniqueId_shouldReturnCorrectUniqueId() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        when(extensionContext.getUniqueId()).thenReturn("[engine:junit-jupiter]/[class:TestClass]/[method:testMethod()]");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getUniqueId()).isEqualTo("[engine:junit-jupiter]/[class:TestClass]/[method:testMethod()]");
    }

    @Test
    void getExtensionContext_shouldReturnSameInstance() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getExtensionContext()).isSameAs(extensionContext);
    }

    @Test
    void constructor_shouldUseDefaultResolverWhenNotProvided() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("testMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        // Should not throw exception
        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context).isNotNull();
        assertThat(context.getParameterName(0)).isNotNull();
    }

    @Test
    void getAnnotations_shouldHandleMethodWithMultipleAnnotations() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("multiAnnotatedMethod");
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        Annotation[] annotations = context.getAnnotations();

        assertThat(annotations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(context.hasAnnotation(Test.class)).isTrue();
        assertThat(context.hasAnnotation(Deprecated.class)).isTrue();
    }

    @Test
    void context_shouldWorkWithParameterizedTest() throws Exception {
        ExtensionContext extensionContext = createMockExtensionContext();
        Method testMethod = TestClassForContext.class.getMethod("parameterizedTestMethod", String.class);
        when(extensionContext.getTestMethod()).thenReturn(Optional.of(testMethod));
        when(extensionContext.getDisplayName()).thenReturn("[1] value1");

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.isValid()).isTrue();
        assertThat(context.hasAnnotation(ParameterizedTest.class)).isTrue();
        assertThat(context.hasAnnotation(ValueSource.class)).isTrue();
    }

    @Test
    void getTestMethod_shouldReturnNullWhenExtensionContextHasNoMethod() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getTestMethod()).isNull();
    }

    @Test
    void context_shouldHandleVeryLongDisplayName() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        String longDisplayName = "Test with very long parameters: " + "a".repeat(500);
        when(extensionContext.getDisplayName()).thenReturn(longDisplayName);

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getDisplayName()).isEqualTo(longDisplayName);
    }

    @Test
    void context_shouldHandleSpecialCharactersInDisplayName() {
        ExtensionContext extensionContext = createMockExtensionContext();
        when(extensionContext.getTestMethod()).thenReturn(Optional.empty());
        String displayName = "Test[♠,♣,♥,♦] with special chars: <>&\"'";
        when(extensionContext.getDisplayName()).thenReturn(displayName);

        ParameterExtractionContext context = new ParameterExtractionContext(extensionContext);

        assertThat(context.getDisplayName()).isEqualTo(displayName);
    }

    private ExtensionContext createMockExtensionContext() {
        return mock(ExtensionContext.class);
    }

    // Helper test class with various method signatures for testing
    static class TestClassForContext {
        public void testMethod() {
        }

        @Test
        public void annotatedMethod() {
        }

        @Test
        @Deprecated
        public void multiAnnotatedMethod() {
        }

        public void methodWithParams(String param1, int param2) {
        }

        @ParameterizedTest
        @ValueSource(strings = {"value1", "value2"})
        public void parameterizedTestMethod(String value) {
        }
    }
}
