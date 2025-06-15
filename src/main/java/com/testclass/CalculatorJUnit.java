package com.testclass;

public class CalculatorJUnit {
    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0) throw new ArithmeticException("Cannot divide by zero");
        return a / b;
    }

    public int modulo(int a, int b) {
        if (b == 0) throw new ArithmeticException("Cannot modulo by zero");
        return a % b;
    }
}
