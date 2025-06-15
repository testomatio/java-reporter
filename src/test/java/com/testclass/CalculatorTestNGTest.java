package com.testclass;

import com.annotation.TestId;
import com.annotation.Title;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CalculatorTestNGTest {

    private CalculatorTestNG calculatorTestNG;

    @BeforeMethod
    public void setUp() {
        calculatorTestNG = new CalculatorTestNG();
    }

    @Test
    @TestId("NG1")
    @Title("Test title NG1")
    public void testAddition() {
        int result = calculatorTestNG.add(2, 3);
        assertEquals(5, result);
    }

    @Test
    @TestId("NG2")
    @Title("Test title NG2")
    public void testSubtraction() {
        int result = calculatorTestNG.subtract(10, 4);
        assertEquals(6, result);
    }

    @Test
    @TestId("NG3")
    @Title("Test title NG3")
    public void testMultiplication() {
        int result = calculatorTestNG.multiply(3, 4);
        assertEquals(12, result);
    }

    @Test
    @TestId("NG4")
    @Title("Test title NG4")
    public void testDivision() {
        int result = calculatorTestNG.divide(12, 4);
        assertEquals(3, result);
    }

    @Test(expectedExceptions = ArithmeticException.class)
    @TestId("NG5")
    @Title("Test title NG5")
    public void testDivideByZero() {
        calculatorTestNG.divide(5, 0);
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
    @Title("Test title NG6")
    public void testAdditionWithDataProvider(int a, int b, int expected) {
        assertEquals(expected, calculatorTestNG.add(a, b));
    }
}
