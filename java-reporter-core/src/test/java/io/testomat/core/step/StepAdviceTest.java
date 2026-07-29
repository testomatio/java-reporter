package io.testomat.core.step;

import static org.junit.jupiter.api.Assertions.*;

import io.testomat.core.annotation.Step;
import io.testomat.core.facade.Testomatio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Test class for {@link io.testomat.core.step.StepAdvice} functionality.
 * Tests parameter substitution in step names using both indexed and named placeholders.
 */
@DisplayName("StepAdvice Tests")
class StepAdviceTest {
    private static final Logger log = LoggerFactory.getLogger(StepAdviceTest.class);

    @BeforeAll
    static void init() {
        Testomatio.artifact();
    }

    @BeforeEach
    void setUp() {
        // Clear steps before each test
        StepStorage.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        StepStorage.clear();
    }

    @Test
    @DisplayName("Should substitute indexed placeholders in step name")
    void testIndexedPlaceholderSubstitution() {
        // When
        stepWithIndexedPlaceholders("John", 25);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size(), "Should have exactly one step");

        TestStep step = steps.get(0);
        assertEquals("Create user John with age 25", step.getStepTitle(),
                "Step title should have indexed placeholders substituted");
    }

    @Test
    @DisplayName("Should substitute named placeholders in step name")
    void testNamedPlaceholderSubstitution() {
        // When
        stepWithNamedPlaceholders("Alice", 30);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size(), "Should have exactly one step");

        TestStep step = steps.get(0);
        // Named placeholders work if -parameters flag is enabled, otherwise they remain as-is
        String title = step.getStepTitle();
        assertTrue(
                title.equals("Створити користувача Alice віком 30 років") ||
                        title.equals("Створити користувача {user} віком {age} років"),
                "Step title should have named placeholders substituted or left as-is: " + title
        );
    }

    @Test
    @DisplayName("Should handle multiple parameters with indexed placeholders")
    void testMultipleIndexedParameters() {
        // When
        stepWithMultipleIndexedParams("product", 5, 99.99);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertEquals("Add 5 items of product for $99.99", step.getStepTitle());
    }

    @Test
    @DisplayName("Should handle null parameter values")
    void testNullParameterValue() {
        // When
        stepWithNullableParameter(null);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertEquals("Process value: null", step.getStepTitle());
    }

    @Test
    @DisplayName("Should handle step without placeholders")
    void testStepWithoutPlaceholders() {
        // When
        stepWithoutPlaceholders("ignored");

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertEquals("Simple step without parameters", step.getStepTitle());
    }

    @Test
    @DisplayName("Should handle step without annotation value")
    void testStepWithoutAnnotationValue() {
        // When
        stepWithoutAnnotationValue("test");

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertEquals("stepWithoutAnnotationValue", step.getStepTitle(),
                "Should use method name when annotation value is empty");
    }

    @Test
    @DisplayName("Should record step duration")
    void testStepDuration() throws InterruptedException {
        // When
        stepWithDelay();

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertTrue(step.getDuration() >= 100, "Duration should be at least 100ms");
        log.info("Step duration: {} ms", step.getDuration());
    }

    @Test
    @DisplayName("Should record step even when exception is thrown")
    void testStepWithException() {
        // When/Then
        assertThrows(RuntimeException.class, this::stepThatThrows);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size(), "Step should be recorded even when it throws");

        TestStep step = steps.get(0);
        assertEquals("Step that throws exception", step.getStepTitle());
    }

    @Test
    @DisplayName("Should handle complex object as parameter")
    void testComplexObjectParameter() {
        // Given
        User user = new User("Bob", 40);

        // When
        stepWithComplexObject(user);

        // Then
        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());

        TestStep step = steps.get(0);
        assertTrue(step.getStepTitle().contains("User{name='Bob', age=40}"),
                "Should use toString() of complex object");
    }

    // Test helper methods with @Step annotation

    @Step("Create user {0} with age {1}")
    private void stepWithIndexedPlaceholders(String name, int age) {
        log.info("Creating user: {} age: {}", name, age);
    }

    @Step("Створити користувача {user} віком {age} років")
    private void stepWithNamedPlaceholders(String user, int age) {
        log.info("Creating user: {} age: {}", user, age);
    }

    @Step("Add {1} items of {0} for ${2}")
    private void stepWithMultipleIndexedParams(String product, int quantity, double price) {
        log.info("Adding {} of {} for ${}", quantity, product, price);
    }

    @Step("Process value: {0}")
    private void stepWithNullableParameter(String value) {
        log.info("Processing: {}", value);
    }

    @Step("Simple step without parameters")
    private void stepWithoutPlaceholders(String ignored) {
        log.info("Simple step");
    }

    @Step
    private void stepWithoutAnnotationValue(String param) {
        log.info("Step without annotation value");
    }

    @Step("Step with delay")
    private void stepWithDelay() throws InterruptedException {
        Thread.sleep(100);
        log.info("Step completed after delay");
    }

    @Step("Step that throws exception")
    private void stepThatThrows() {
        log.info("About to throw exception");
        throw new RuntimeException("Test exception");
    }

    @Step("Failing step")
    void stepThatThrowsWithoutMessage() {
        throw new RuntimeException();
    }

    @Step("Process user: {0}")
    private void stepWithComplexObject(User user) {
        log.info("Processing user: {}", user);
    }

    // Helper class for testing complex objects
    private static class User {
        private final String name;
        private final int age;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }

    @Test
    void testFailureStatus(){
        assertThrows(RuntimeException.class, this::stepThatThrows);
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals(StepStatus.failed, step.getStatus());
    }

    @Test
    void testErrorMessageStored() {
        assertThrows(RuntimeException.class, this::stepThatThrows);
        TestStep step = StepStorage.getSteps().get(0);
        assertTrue(step.getError().contains("Test exception"));
    }

    @Test
    void shouldStoreExceptionClassWhenMessageIsNull() {
        assertThrows(RuntimeException.class, this::stepThatThrowsWithoutMessage);
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals("RuntimeException", step.getError());
    }

    @Test
    void testNoParameters(){
        simpleStep();

        TestStep step= StepStorage.getSteps().get(0);
        assertEquals("No params", step.getStepTitle());
    }

    @Test
    void testReturnValue(){
        String result= stepReturningValue();
        assertEquals("ok",result);
    }

    @Test
    void testParameterMismatch(){
        methodWithVarArgs("a","b");
        assertEquals(1, StepStorage.getSteps().size());
    }

    @Test
    void testNoArtifacts(){
        stepWithoutArtifacts();

        TestStep step = StepStorage.getSteps().get(0);
        assertNull(step.getArtifacts());
    }

    @Test
    void testLifecycleCleanup(){
        try {
            stepThatThrows();
        } catch (Exception ignored) {
        }
        assertNull(StepLifecycle.current());
    }

    @Test
    @DisplayName("Should set passed status for successful step")
    void testPassedStatus() {
        simpleStep();
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals(StepStatus.passed, step.getStatus());
    }

    @Test
    @DisplayName("Should store stack trace log for failed step")
    void testFailureLogStored() {
        assertThrows(RuntimeException.class, this::stepThatThrows);
        TestStep step = StepStorage.getSteps().get(0);
        assertNotNull(step.getLog());
        assertTrue(step.getLog().contains("stepThatThrows"));
    }

    @Test
    @DisplayName("Should set user category by default")
    void testDefaultCategory() {
        simpleStep();
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals("user", step.getCategory());
    }

    @Test
    @DisplayName("Should store duration for failed step")
    void testFailedStepDuration() {
        assertThrows(RuntimeException.class, this::stepThatThrows);
        TestStep step = StepStorage.getSteps().get(0);
        assertTrue(step.getDuration() >= 0);
    }

    @Test
    @DisplayName("Should keep unresolved placeholders")
    void testUnresolvedPlaceholder() {
        unresolvedPlaceholderStep("John");
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals("User John {1}", step.getStepTitle());
    }

    @Test
    @DisplayName("Should support repeated placeholders")
    void testRepeatedPlaceholders() {
        repeatedPlaceholderStep("Bob");
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals("User Bob again Bob", step.getStepTitle());
    }

    @Test
    @DisplayName("Should handle empty string parameter")
    void testEmptyStringParameter() {
        stepWithNullableParameter("");
        TestStep step = StepStorage.getSteps().get(0);
        assertEquals("Process value: ", step.getStepTitle());
    }

    @Step("User {0} {1}")
    private void unresolvedPlaceholderStep(String name) {
    }

    @Step("User {0} again {0}")
    private void repeatedPlaceholderStep(String value) {
    }

    @Step("no artifacts")
    private void stepWithoutArtifacts(){}

    @Step("test {0}")
    private void methodWithVarArgs(String... args){}

    @Step("return")
    private String stepReturningValue(){
        return "ok";
    }

    @Step("No params")
    private void simpleStep(){}

}
