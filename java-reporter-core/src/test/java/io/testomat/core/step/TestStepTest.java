package io.testomat.core.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TestStep Tests")
class TestStepTest {

    @Test
    @DisplayName("Should create TestStep with default values")
    void testDefaultConstructor() {
        TestStep step = new TestStep();

        assertNull(step.getCategory());
        assertNull(step.getStepTitle());
        assertEquals(0.0, step.getDuration());
        assertNotNull(step.getSubsteps());
        assertTrue(step.getSubsteps().isEmpty());
    }

    @Test
    @DisplayName("Should set and get category")
    void testCategoryGetterSetter() {
        TestStep step = new TestStep();
        step.setCategory("action");

        assertEquals("action", step.getCategory());
    }

    @Test
    @DisplayName("Should set and get step title")
    void testStepTitleGetterSetter() {
        TestStep step = new TestStep();
        step.setStepTitle("Login to application");

        assertEquals("Login to application", step.getStepTitle());
    }

    @Test
    @DisplayName("Should set and get duration")
    void testDurationGetterSetter() {
        TestStep step = new TestStep();
        step.setDuration(150.5);

        assertEquals(150.5, step.getDuration());
    }

    @Test
    @DisplayName("Should handle substeps")
    void testSubsteps() {
        TestStep parentStep = new TestStep();
        parentStep.setStepTitle("Parent Step");

        TestStep childStep1 = new TestStep();
        childStep1.setStepTitle("Child Step 1");

        TestStep childStep2 = new TestStep();
        childStep2.setStepTitle("Child Step 2");

        List<TestStep> substeps = Arrays.asList(childStep1, childStep2);
        parentStep.setSubsteps(substeps);

        assertEquals(2, parentStep.getSubsteps().size());
        assertEquals("Child Step 1", parentStep.getSubsteps().get(0).getStepTitle());
        assertEquals("Child Step 2", parentStep.getSubsteps().get(1).getStepTitle());
    }

    @Test
    @DisplayName("Should handle nested substeps")
    void testNestedSubsteps() {
        TestStep grandparentStep = new TestStep();
        grandparentStep.setStepTitle("Grandparent Step");

        TestStep parentStep = new TestStep();
        parentStep.setStepTitle("Parent Step");

        TestStep childStep = new TestStep();
        childStep.setStepTitle("Child Step");

        parentStep.setSubsteps(Arrays.asList(childStep));
        grandparentStep.setSubsteps(Arrays.asList(parentStep));

        assertEquals(1, grandparentStep.getSubsteps().size());
        assertEquals("Parent Step", grandparentStep.getSubsteps().get(0).getStepTitle());
        assertEquals(1, grandparentStep.getSubsteps().get(0).getSubsteps().size());
        assertEquals("Child Step",
                grandparentStep.getSubsteps().get(0).getSubsteps().get(0).getStepTitle());
    }

    @Test
    @DisplayName("Should handle empty substeps list")
    void testEmptySubstepsList() {
        TestStep step = new TestStep();
        step.setStepTitle("Step with no substeps");
        step.setSubsteps(new ArrayList<>());

        assertNotNull(step.getSubsteps());
        assertTrue(step.getSubsteps().isEmpty());
    }

    @Test
    @DisplayName("Should handle different categories")
    void testDifferentCategories() {
        TestStep actionStep = new TestStep();
        actionStep.setCategory("action");
        actionStep.setStepTitle("Click button");

        TestStep verifyStep = new TestStep();
        verifyStep.setCategory("verify");
        verifyStep.setStepTitle("Verify result");

        TestStep setupStep = new TestStep();
        setupStep.setCategory("setup");
        setupStep.setStepTitle("Initialize data");

        assertEquals("action", actionStep.getCategory());
        assertEquals("verify", verifyStep.getCategory());
        assertEquals("setup", setupStep.getCategory());
    }

    @Test
    @DisplayName("Should handle zero duration")
    void testZeroDuration() {
        TestStep step = new TestStep();
        step.setStepTitle("Instant step");
        step.setDuration(0.0);

        assertEquals(0.0, step.getDuration());
    }

    @Test
    @DisplayName("Should handle large duration values")
    void testLargeDuration() {
        TestStep step = new TestStep();
        step.setStepTitle("Long running step");
        step.setDuration(999999.99);

        assertEquals(999999.99, step.getDuration());
    }

    @Test
    @DisplayName("Should handle special characters in step title")
    void testSpecialCharactersInTitle() {
        TestStep step = new TestStep();
        String specialTitle = "Step with \"quotes\" and special chars: @#$%";
        step.setStepTitle(specialTitle);

        assertEquals(specialTitle, step.getStepTitle());
    }

    @Test
    @DisplayName("Should update step properties")
    void testUpdateStepProperties() {
        TestStep step = new TestStep();

        step.setCategory("initial");
        step.setStepTitle("Initial Title");
        step.setDuration(100.0);

        assertEquals("initial", step.getCategory());
        assertEquals("Initial Title", step.getStepTitle());
        assertEquals(100.0, step.getDuration());

        step.setCategory("updated");
        step.setStepTitle("Updated Title");
        step.setDuration(200.0);

        assertEquals("updated", step.getCategory());
        assertEquals("Updated Title", step.getStepTitle());
        assertEquals(200.0, step.getDuration());
    }

    @Test
    @DisplayName("Should handle null category")
    void testNullCategory() {
        TestStep step = new TestStep();
        step.setCategory(null);

        assertNull(step.getCategory());
    }

    @Test
    @DisplayName("Should handle null step title")
    void testNullStepTitle() {
        TestStep step = new TestStep();
        step.setStepTitle(null);

        assertNull(step.getStepTitle());
    }

    @Test
    @DisplayName("Should replace substeps list")
    void testReplaceSubsteps() {
        TestStep step = new TestStep();

        TestStep substep1 = new TestStep();
        substep1.setStepTitle("Substep 1");
        step.setSubsteps(Arrays.asList(substep1));

        assertEquals(1, step.getSubsteps().size());

        TestStep substep2 = new TestStep();
        substep2.setStepTitle("Substep 2");
        TestStep substep3 = new TestStep();
        substep3.setStepTitle("Substep 3");
        step.setSubsteps(Arrays.asList(substep2, substep3));

        assertEquals(2, step.getSubsteps().size());
        assertEquals("Substep 2", step.getSubsteps().get(0).getStepTitle());
        assertEquals("Substep 3", step.getSubsteps().get(1).getStepTitle());
    }
}
