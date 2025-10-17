package io.testomat.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.testomat.core.step.TestStep;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TestResult Tests")
class TestResultTest {

    @Test
    @DisplayName("Should create TestResult with constructor")
    void testConstructor() {
        TestStep step = new TestStep();
        step.setStepTitle("Step 1");
        List<TestStep> steps = Arrays.asList(step);

        TestResult result = new TestResult(
                "Test Title",
                "test-123",
                "Suite Title",
                "TestFile.java",
                "passed",
                "Success message",
                "stack trace",
                "example data",
                "rid-456",
                steps
        );

        assertEquals("Test Title", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("Suite Title", result.getSuiteTitle());
        assertEquals("TestFile.java", result.getFile());
        assertEquals("passed", result.getStatus());
        assertEquals("Success message", result.getMessage());
        assertEquals("stack trace", result.getStack());
        assertEquals("example data", result.getExample());
        assertEquals("rid-456", result.getRid());
        assertEquals(1, result.getSteps().size());
        assertEquals("Step 1", result.getSteps().get(0).getStepTitle());
    }

    @Test
    @DisplayName("Should create TestResult with builder pattern")
    void testBuilder() {
        TestStep step = new TestStep();
        step.setStepTitle("Step 1");
        List<TestStep> steps = Arrays.asList(step);

        TestResult result = TestResult.builder()
                .withTitle("Test Title")
                .withTestId("test-123")
                .withSuiteTitle("Suite Title")
                .withFile("TestFile.java")
                .withStatus("passed")
                .withMessage("Success message")
                .withStack("stack trace")
                .withExample("example data")
                .withRid("rid-456")
                .withSteps(steps)
                .build();

        assertEquals("Test Title", result.getTitle());
        assertEquals("test-123", result.getTestId());
        assertEquals("Suite Title", result.getSuiteTitle());
        assertEquals("TestFile.java", result.getFile());
        assertEquals("passed", result.getStatus());
        assertEquals("Success message", result.getMessage());
        assertEquals("stack trace", result.getStack());
        assertEquals("example data", result.getExample());
        assertEquals("rid-456", result.getRid());
        assertNotNull(result.getSteps());
        assertEquals(1, result.getSteps().size());
    }

    @Test
    @DisplayName("Should create TestResult with minimal required fields")
    void testBuilderMinimalFields() {
        TestResult result = TestResult.builder()
                .withTitle("Test Title")
                .withSuiteTitle("Suite Title")
                .withFile("TestFile.java")
                .withStatus("passed")
                .build();

        assertEquals("Test Title", result.getTitle());
        assertEquals("Suite Title", result.getSuiteTitle());
        assertEquals("TestFile.java", result.getFile());
        assertEquals("passed", result.getStatus());
        assertNull(result.getTestId());
        assertNull(result.getMessage());
        assertNull(result.getStack());
        assertNull(result.getExample());
        assertNull(result.getRid());
        assertNull(result.getSteps());
    }

    @Test
    @DisplayName("Should use setters to modify TestResult")
    void testSetters() {
        TestResult result = new TestResult();

        result.setTitle("Updated Title");
        result.setTestId("updated-123");
        result.setSuiteTitle("Updated Suite");
        result.setFile("UpdatedFile.java");
        result.setStatus("failed");
        result.setMessage("Failure message");
        result.setStack("updated stack");
        result.setExample("updated example");
        result.setRid("updated-rid");

        TestStep step = new TestStep();
        step.setStepTitle("Updated Step");
        result.setSteps(Arrays.asList(step));

        assertEquals("Updated Title", result.getTitle());
        assertEquals("updated-123", result.getTestId());
        assertEquals("Updated Suite", result.getSuiteTitle());
        assertEquals("UpdatedFile.java", result.getFile());
        assertEquals("failed", result.getStatus());
        assertEquals("Failure message", result.getMessage());
        assertEquals("updated stack", result.getStack());
        assertEquals("updated example", result.getExample());
        assertEquals("updated-rid", result.getRid());
        assertEquals(1, result.getSteps().size());
        assertEquals("Updated Step", result.getSteps().get(0).getStepTitle());
    }

    @Test
    @DisplayName("Should handle different test statuses")
    void testDifferentStatuses() {
        TestResult passed = TestResult.builder()
                .withTitle("Passed Test")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("passed")
                .build();

        TestResult failed = TestResult.builder()
                .withTitle("Failed Test")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("failed")
                .withMessage("Test failed")
                .withStack("at Test.java:10")
                .build();

        TestResult skipped = TestResult.builder()
                .withTitle("Skipped Test")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("skipped")
                .build();

        assertEquals("passed", passed.getStatus());
        assertEquals("failed", failed.getStatus());
        assertEquals("skipped", skipped.getStatus());
        assertNotNull(failed.getMessage());
        assertNotNull(failed.getStack());
    }

    @Test
    @DisplayName("Should handle example data for parameterized tests")
    void testExampleData() {
        Object exampleData = Arrays.asList("param1", "param2", "param3");

        TestResult result = TestResult.builder()
                .withTitle("Parameterized Test")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("passed")
                .withExample(exampleData)
                .build();

        assertNotNull(result.getExample());
        assertEquals(exampleData, result.getExample());
    }

    @Test
    @DisplayName("Should handle multiple test steps")
    void testMultipleSteps() {
        TestStep step1 = new TestStep();
        step1.setStepTitle("Step 1");
        step1.setDuration(100);

        TestStep step2 = new TestStep();
        step2.setStepTitle("Step 2");
        step2.setDuration(200);

        List<TestStep> steps = Arrays.asList(step1, step2);

        TestResult result = TestResult.builder()
                .withTitle("Test with Steps")
                .withSuiteTitle("Suite")
                .withFile("Test.java")
                .withStatus("passed")
                .withSteps(steps)
                .build();

        assertEquals(2, result.getSteps().size());
        assertEquals("Step 1", result.getSteps().get(0).getStepTitle());
        assertEquals("Step 2", result.getSteps().get(1).getStepTitle());
        assertEquals(100, result.getSteps().get(0).getDuration());
        assertEquals(200, result.getSteps().get(1).getDuration());
    }

    @Test
    @DisplayName("Should create empty TestResult with default constructor")
    void testDefaultConstructor() {
        TestResult result = new TestResult();

        assertNull(result.getTitle());
        assertNull(result.getTestId());
        assertNull(result.getSuiteTitle());
        assertNull(result.getFile());
        assertNull(result.getStatus());
        assertNull(result.getMessage());
        assertNull(result.getStack());
        assertNull(result.getExample());
        assertNull(result.getRid());
        assertNull(result.getSteps());
    }
}
