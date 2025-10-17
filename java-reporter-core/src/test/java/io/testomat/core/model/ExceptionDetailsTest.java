package io.testomat.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ExceptionDetails Tests")
class ExceptionDetailsTest {

    @Test
    @DisplayName("Should create ExceptionDetails with message and stack")
    void testConstructorWithMessageAndStack() {
        String message = "Test exception message";
        String stack = "at TestClass.method(TestClass.java:10)\n"
                + "at TestRunner.run(TestRunner.java:20)";

        ExceptionDetails details = new ExceptionDetails(message, stack);

        assertEquals(message, details.getMessage());
        assertEquals(stack, details.getStack());
    }

    @Test
    @DisplayName("Should create ExceptionDetails with null message")
    void testConstructorWithNullMessage() {
        String stack = "at TestClass.method(TestClass.java:10)";

        ExceptionDetails details = new ExceptionDetails(null, stack);

        assertNull(details.getMessage());
        assertEquals(stack, details.getStack());
    }

    @Test
    @DisplayName("Should create ExceptionDetails with null stack")
    void testConstructorWithNullStack() {
        String message = "Test exception message";

        ExceptionDetails details = new ExceptionDetails(message, null);

        assertEquals(message, details.getMessage());
        assertNull(details.getStack());
    }

    @Test
    @DisplayName("Should create empty ExceptionDetails")
    void testEmptyExceptionDetails() {
        ExceptionDetails details = ExceptionDetails.empty();

        assertNotNull(details);
        assertNull(details.getMessage());
        assertNull(details.getStack());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        ExceptionDetails details = new ExceptionDetails("", "");

        assertNotNull(details);
        assertEquals("", details.getMessage());
        assertEquals("", details.getStack());
    }

    @Test
    @DisplayName("Should handle multiline stack trace")
    void testMultilineStackTrace() {
        String message = "NullPointerException";
        String stack = "java.lang.NullPointerException: Cannot invoke method\n"
                + "\tat com.example.TestClass.testMethod(TestClass.java:42)\n"
                + "\tat com.example.TestRunner.runTest(TestRunner.java:100)\n"
                + "\tat org.junit.runners.JUnit4.run(JUnit4.java:137)";

        ExceptionDetails details = new ExceptionDetails(message, stack);

        assertEquals(message, details.getMessage());
        assertEquals(stack, details.getStack());
    }

    @Test
    @DisplayName("Should handle long error messages")
    void testLongErrorMessage() {
        String longMessage = "Expected value was 'expected' but actual value was 'actual'. "
                + "This is a detailed error message that provides context about the failure. "
                + "Additional information: The test failed because the expected condition was not met.";
        String stack = "at TestClass.java:10";

        ExceptionDetails details = new ExceptionDetails(longMessage, stack);

        assertEquals(longMessage, details.getMessage());
        assertEquals(stack, details.getStack());
    }

    @Test
    @DisplayName("Should handle special characters in message")
    void testSpecialCharactersInMessage() {
        String message = "Test failed with \"quotes\" and special chars: <>&\nNew line\tTab";
        String stack = "at TestClass.java:10";

        ExceptionDetails details = new ExceptionDetails(message, stack);

        assertEquals(message, details.getMessage());
        assertEquals(stack, details.getStack());
    }
}
