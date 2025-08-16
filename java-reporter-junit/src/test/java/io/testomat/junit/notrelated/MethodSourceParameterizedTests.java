package io.testomat.junit.notrelated;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.junit.extractor.InvocationInterceptor;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(InvocationInterceptor.class)
public class MethodSourceParameterizedTests {

    static Stream<String> provideStringValues() {
        return Stream.of("apple", "banana", "cherry", "apple", "banana", "cherry", "apple", "banana", "cherry");
    }

    static Stream<Integer> provideIntegerValues() {
        return Stream.of(10, 20, 30, 10, 20, 30, 10, 20, 30);
    }

    static Stream<Arguments> provideStringAndInteger() {
        return Stream.of(
            Arguments.of("test1", 1),
            Arguments.of("test2", 2),
            Arguments.of("test3", 3),
            Arguments.of("test1", 1),
            Arguments.of("test2", 2),
            Arguments.of("test3", 3),
            Arguments.of("test1", 1),
            Arguments.of("test2", 2),
            Arguments.of("test3", 3)
        );
    }

    static Stream<Arguments> provideComplexTestData() {
        return Stream.of(
            Arguments.of("John", 25, true),
            Arguments.of("Jane", 30, false),
            Arguments.of("Bob", 35, true),
            Arguments.of("John", 25, true),
            Arguments.of("Jane", 30, false),
            Arguments.of("Bob", 35, true),
            Arguments.of("John", 25, true),
            Arguments.of("Jane", 30, false),
            Arguments.of("Bob", 35, true)
        );
    }

    static Stream<Double> provideDoubleValues() {
        return Stream.of(1.1, 2.2, 3.3, 1.1, 2.2, 3.3, 1.1, 2.2, 3.3);
    }

    @ParameterizedTest
    @MethodSource("provideStringValues")
    void testStringMethodSource(String value) {
        assertNotNull(value);
        assertTrue(value.length() > 0);
        assertTrue(value.matches("[a-z]+"));
    }

    @ParameterizedTest
    @MethodSource("provideIntegerValues")
    void testIntegerMethodSource(Integer value) {
        assertNotNull(value);
        assertTrue(value > 0);
        assertEquals(0, value % 10);
    }

    @ParameterizedTest
    @MethodSource("provideStringAndInteger")
    void testMultipleParametersMethodSource(String text, Integer number) {
        assertNotNull(text);
        assertNotNull(number);
        assertTrue(text.startsWith("test"));
        assertTrue(number > 0 && number <= 3);
    }

    @ParameterizedTest
    @MethodSource("provideComplexTestData")
    void testComplexDataMethodSource(String name, Integer age, Boolean active) {
        assertNotNull(name);
        assertNotNull(age);
        assertNotNull(active);
        assertTrue(name.length() >= 3);
        assertTrue(age >= 18);
    }

    @ParameterizedTest
    @MethodSource("provideDoubleValues")
    void testDoubleMethodSource(Double value) {
        assertNotNull(value);
        assertTrue(value > 0.0);
        assertTrue(value < 10.0);
    }
}