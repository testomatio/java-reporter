package com.testclass;

import com.annotation.TestId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CalculatorTest {

    private Calculator calculator;

    @BeforeMethod
    public void setUp() {
        calculator = new Calculator();
    }

    @Test
    @TestId("NG1")
    public void testAddition() {
        int result = calculator.add(2, 3);
        assertEquals(result, 5);
    }

    @Test
    @TestId("NG2")
    public void testSubtraction() {
        int result = calculator.subtract(10, 4);
        assertEquals(result, 6);
    }

    @Test
    @TestId("NG3")
    public void testMultiplication() {
        int result = calculator.multiply(3, 4);
        assertEquals(result, 12);
    }

    @Test
    @TestId("NG4")
    public void testDivision() {
        int result = calculator.divide(12, 4);
        assertEquals(result, 3);
    }

    @Test(expectedExceptions = ArithmeticException.class)
    @TestId("NG5")
    public void testDivideByZero() {
        calculator.divide(5, 0);
    }

    @DataProvider(name = "addData")
    public Object[][] addData() {
        return new Object[][]{
                {1, 2, 3},
                {0, 0, 0},
                {-1, -1, -2},
        };
    }

    @Test(dataProvider = "addData")
    @TestId("NG6")
    public void testAdditionWithDataProvider(int a, int b, int expected) {
        assertEquals(calculator.add(a, b), expected);
    }
}
