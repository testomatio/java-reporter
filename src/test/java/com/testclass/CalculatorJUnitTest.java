package com.testclass;

import com.annotation.TestId;
import com.annotation.Title;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorJUnitTest {

    private final CalculatorJUnit calculator = new CalculatorJUnit();

    @Test
    @TestId("JUnit1")
    @Title("Test title JUnit1")
    void testAddition() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    @TestId("JUnit2")
    @Title("Test title JUnit2")
    void testSubtraction() {
        assertEquals(1, calculator.subtract(4, 3));
    }

    @Test
    @TestId("JUnit3")
    @Title("Test title JUnit3")
    void testMultiplication() {
        assertEquals(12, calculator.multiply(4, 3));
    }

    @Test
    @TestId("JUnit4")
    @Title("Test title JUnit4")
    void testDivision() {
        assertEquals(2, calculator.divide(10, 5));
    }

    @Test
    @TestId("JUnit5")
    @Title("Test title JUnit5")
    void testDivisionByZeroThrowsException() {
        assertThrows(ArithmeticException.class, () -> calculator.divide(5, 0));
    }

    @Test
    @TestId("JUnit6")
    @Title("Test title JUnit6")
    void testModulo() {
        assertEquals(1, calculator.modulo(10, 3));
    }

    @Test
    @TestId("JUnit7")
    @Title("Test title JUnit7")
    void testModuloByZeroThrowsException() {
        assertThrows(ArithmeticException.class, () -> calculator.modulo(5, 0));
    }

    @Test
    @TestId("JUnit8")
    @Title("Test title JUnit8")
    void testNegativeAddition() {
        assertEquals(-3, calculator.add(-1, -2));
    }

    @Test
    @TestId("JUnit9")
    @Title("Test title JUnit9")
    void testNegativeMultiplication() {
        assertEquals(-6, calculator.multiply(-2, 3));
    }

    @Test
    @TestId("JUnit10")
    @Title("Test title JUnit10")
    void testZeroMultiplication() {
        assertEquals(0, calculator.multiply(0, 1000));
    }
}