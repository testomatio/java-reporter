package io.testomat.junit.notrelated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.junit.extractor.InvocationInterceptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(InvocationInterceptor.class)
public class ValueSourceParameterizedTests {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 1, 2, 3, 1, 2, 3})
    void testPositiveIntegerValidation(int value) {
        assertTrue(value > 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", "junit", "hello", "world", "junit", "hello", "world", "junit"})
    void testStringLengthValidation(String input) {
        assertTrue(input.length() > 0);
        assertFalse(input.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.5, 2.7, 3.14, 1.5, 2.7, 3.14, 1.5, 2.7, 3.14})
    void testDoubleRangeValidation(double value) {
        assertTrue(value > 1.0);
        assertTrue(value < 10.0);
    }

    @ParameterizedTest
    @ValueSource(longs = {100L, 200L, 300L, 100L, 200L, 300L, 100L, 200L, 300L})
    void testLongNumberValidation(long number) {
        assertTrue(number >= 100L);
        assertEquals(0, number % 100);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false, true, false, true, false})
    void testBooleanLogicValidation(boolean flag) {
        assertNotNull(flag);
        assertTrue(flag || !flag);
    }
}