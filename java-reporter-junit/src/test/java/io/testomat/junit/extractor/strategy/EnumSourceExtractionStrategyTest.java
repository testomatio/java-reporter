package io.testomat.junit.extractor.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for EnumSourceExtractionStrategy to verify parameter extraction from @EnumSource annotations.
 */
@DisplayName("EnumSourceExtractionStrategy Tests")
class EnumSourceExtractionStrategyTest {

    private EnumSourceExtractionStrategy strategy;
    private ExtensionContext mockContext;

    @BeforeEach
    void setUp() {
        strategy = new EnumSourceExtractionStrategy();
        mockContext = mock(ExtensionContext.class);
    }

    @Test
    @DisplayName("Should support methods with @EnumSource annotation")
    void shouldSupportEnumSourceAnnotation() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("dayOfWeekEnumTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertTrue(supports);
    }

    @Test
    @DisplayName("Should not support methods without @EnumSource annotation")
    void shouldNotSupportNonEnumSourceMethods() throws NoSuchMethodException {
        // Given
        Method method = TestMethodHolder.class.getMethod("regularTest");
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("regularTest");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        boolean supports = strategy.supports(context);

        // Then
        assertFalse(supports);
    }

    @Test
    @DisplayName("Should extract enum parameter from display name")
    void shouldExtractEnumParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] MONDAY");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(DayOfWeek.MONDAY, paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract enum parameter with class prefix from display name")
    void shouldExtractEnumParameterWithClassPrefixFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[2] DayOfWeek.FRIDAY");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(DayOfWeek.FRIDAY, paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract TimeUnit enum parameter from display name")
    void shouldExtractTimeUnitEnumParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("timeUnitEnumTest", TimeUnit.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] SECONDS");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(TimeUnit.SECONDS, paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle null parameter from display name")
    void shouldHandleNullParameterFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] null");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertNull(paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should extract enum with names filter from annotation")
    void shouldExtractEnumWithNamesFilterFromAnnotation() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekWithNamesFilterTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("unparseable display name");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(DayOfWeek.MONDAY, paramMap.get(context.getParameterName(0))); // Should get first from names filter
    }

    @Test
    @DisplayName("Should extract enum without explicit class specification")
    void shouldExtractEnumWithoutExplicitClassSpecification() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("implicitEnumTypeTest", TestEnum.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("unparseable display name");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        
        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(TestEnum.VALUE1, paramMap.get(context.getParameterName(0))); // Should get first enum constant
    }

    @Test
    @DisplayName("Should handle custom enum parsing from display name")
    void shouldHandleCustomEnumParsingFromDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("implicitEnumTypeTest", TestEnum.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] VALUE2");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(TestEnum.VALUE2, paramMap.get(context.getParameterName(0)));
    }

    @Test
    @DisplayName("Should handle invalid enum name gracefully")
    void shouldHandleInvalidEnumNameGracefully() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("[1] INVALID_DAY");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals("INVALID_DAY", paramMap.get(context.getParameterName(0))); // Should return as string when enum parsing fails
    }

    @Test
    @DisplayName("Should handle empty display name")
    void shouldHandleEmptyDisplayName() throws Exception {
        // Given
        Method method = TestMethodHolder.class.getMethod("dayOfWeekEnumTest", DayOfWeek.class);
        when(mockContext.getTestMethod()).thenReturn(Optional.of(method));
        when(mockContext.getDisplayName()).thenReturn("");
        when(mockContext.getUniqueId()).thenReturn("test-id");

        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> paramMap = (Map<String, Object>) result;
        assertEquals(DayOfWeek.MONDAY, paramMap.get(context.getParameterName(0))); // Should fallback to annotation
    }

    @Test
    @DisplayName("Should return correct strategy name")
    void shouldReturnCorrectStrategyName() {
        // When
        String name = strategy.getStrategyName();

        // Then
        assertEquals("EnumSourceExtractionStrategy", name);
    }

    @Test
    @DisplayName("Should return correct priority")
    void shouldReturnCorrectPriority() {
        // When
        int priority = strategy.getPriority();

        // Then
        assertEquals(10, priority);
    }

    @Test
    @DisplayName("Should handle invalid context gracefully")
    void shouldHandleInvalidContextGracefully() throws Exception {
        // Given
        when(mockContext.getTestMethod()).thenReturn(Optional.empty());
        ParameterExtractionContext context = new ParameterExtractionContext(mockContext);

        // When
        Object result = strategy.extractParameters(context);

        // Then
        assertNull(result);
    }

    // Test helper enum
    public enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    // Test helper class with methods for reflection
    public static class TestMethodHolder {

        @ParameterizedTest
        @EnumSource(DayOfWeek.class)
        public void dayOfWeekEnumTest(DayOfWeek weekday) {
            // Test method for reflection
        }

        @ParameterizedTest
        @EnumSource(TimeUnit.class)
        public void timeUnitEnumTest(TimeUnit timeUnit) {
            // Test method for reflection
        }

        @ParameterizedTest
        @EnumSource(value = DayOfWeek.class, names = {"MONDAY", "TUESDAY", "WEDNESDAY"})
        public void dayOfWeekWithNamesFilterTest(DayOfWeek selectedDay) {
            // Test method for reflection with names filter
        }

        @ParameterizedTest
        @EnumSource // No explicit enum class - should be inferred from parameter type
        public void implicitEnumTypeTest(TestEnum enumValue) {
            // Test method for reflection with implicit enum type
        }

        public void regularTest() {
            // Regular test method without parameterization
        }
    }
}