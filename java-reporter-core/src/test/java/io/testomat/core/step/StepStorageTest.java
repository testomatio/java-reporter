package io.testomat.core.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StepStorage Tests")
class StepStorageTest {

    @AfterEach
    void cleanup() {
        StepStorage.clear();
    }

    @Test
    @DisplayName("Should add and retrieve steps")
    void testAddAndGetSteps() {
        TestStep step1 = new TestStep();
        step1.setStepTitle("Step 1");

        TestStep step2 = new TestStep();
        step2.setStepTitle("Step 2");

        StepStorage.addStep(step1);
        StepStorage.addStep(step2);

        List<TestStep> steps = StepStorage.getSteps();

        assertEquals(2, steps.size());
        assertEquals("Step 1", steps.get(0).getStepTitle());
        assertEquals("Step 2", steps.get(1).getStepTitle());
    }

    @Test
    @DisplayName("Should return empty list when no steps added")
    void testGetStepsWhenEmpty() {
        List<TestStep> steps = StepStorage.getSteps();

        assertNotNull(steps);
        assertTrue(steps.isEmpty());
    }

    @Test
    @DisplayName("Should clear all steps")
    void testClearSteps() {
        TestStep step = new TestStep();
        step.setStepTitle("Step 1");

        StepStorage.addStep(step);
        assertEquals(1, StepStorage.getSteps().size());

        StepStorage.clear();
        assertTrue(StepStorage.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Should return independent copy of steps list")
    void testGetStepsReturnsIndependentCopy() {
        TestStep step1 = new TestStep();
        step1.setStepTitle("Step 1");
        StepStorage.addStep(step1);

        List<TestStep> steps1 = StepStorage.getSteps();
        List<TestStep> steps2 = StepStorage.getSteps();

        assertEquals(steps1.size(), steps2.size());

        // Modifying returned list should not affect storage
        TestStep extraStep = new TestStep();
        extraStep.setStepTitle("Extra Step");
        steps1.add(extraStep);

        assertEquals(2, steps1.size());
        assertEquals(1, StepStorage.getSteps().size());
    }

    @Test
    @DisplayName("Should handle multiple add and clear operations")
    void testMultipleAddAndClearOperations() {
        // First batch
        TestStep step1 = new TestStep();
        step1.setStepTitle("Step 1");
        StepStorage.addStep(step1);
        assertEquals(1, StepStorage.getSteps().size());

        StepStorage.clear();
        assertTrue(StepStorage.getSteps().isEmpty());

        // Second batch
        TestStep step2 = new TestStep();
        step2.setStepTitle("Step 2");
        TestStep step3 = new TestStep();
        step3.setStepTitle("Step 3");
        StepStorage.addStep(step2);
        StepStorage.addStep(step3);
        assertEquals(2, StepStorage.getSteps().size());

        StepStorage.clear();
        assertTrue(StepStorage.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Should maintain step order")
    void testStepOrder() {
        for (int i = 0; i < 10; i++) {
            TestStep step = new TestStep();
            step.setStepTitle("Step " + i);
            StepStorage.addStep(step);
        }

        List<TestStep> steps = StepStorage.getSteps();

        assertEquals(10, steps.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("Step " + i, steps.get(i).getStepTitle());
        }
    }

    @Test
    @DisplayName("Should handle steps with substeps")
    void testStepsWithSubsteps() {
        TestStep parentStep = new TestStep();
        parentStep.setStepTitle("Parent Step");

        TestStep childStep = new TestStep();
        childStep.setStepTitle("Child Step");
        parentStep.setSubsteps(List.of(childStep));

        StepStorage.addStep(parentStep);

        List<TestStep> steps = StepStorage.getSteps();

        assertEquals(1, steps.size());
        assertEquals("Parent Step", steps.get(0).getStepTitle());
        assertEquals(1, steps.get(0).getSubsteps().size());
        assertEquals("Child Step", steps.get(0).getSubsteps().get(0).getStepTitle());
    }

    @Test
    @DisplayName("Should be thread-safe for concurrent access")
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int stepsPerThread = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger[] threadStepCounts = new AtomicInteger[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threadStepCounts[i] = new AtomicInteger(0);
        }

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int i = 0; i < stepsPerThread; i++) {
                        TestStep step = new TestStep();
                        step.setStepTitle("Thread-" + threadId + "-Step-" + i);
                        StepStorage.addStep(step);
                    }

                    // Each thread stores its own step count
                    threadStepCounts[threadId].set(StepStorage.getSteps().size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // Start all threads
        doneLatch.await(); // Wait for all threads to finish

        // Verify each thread had its own isolated storage with correct count
        for (int i = 0; i < threadCount; i++) {
            assertEquals(stepsPerThread, threadStepCounts[i].get(),
                    "Thread " + i + " should have " + stepsPerThread + " steps");
        }

        // Main thread should have empty storage
        assertTrue(StepStorage.getSteps().isEmpty(),
                "Main thread should have no steps due to ThreadLocal isolation");
    }

    @Test
    @DisplayName("Should isolate steps between threads")
    void testThreadLocalIsolation() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger thread1Count = new AtomicInteger(0);
        AtomicInteger thread2Count = new AtomicInteger(0);

        Thread thread1 = new Thread(() -> {
            TestStep step1 = new TestStep();
            step1.setStepTitle("Thread 1 - Step 1");
            StepStorage.addStep(step1);

            TestStep step2 = new TestStep();
            step2.setStepTitle("Thread 1 - Step 2");
            StepStorage.addStep(step2);

            thread1Count.set(StepStorage.getSteps().size());
            latch.countDown();
        });

        Thread thread2 = new Thread(() -> {
            TestStep step = new TestStep();
            step.setStepTitle("Thread 2 - Step 1");
            StepStorage.addStep(step);

            thread2Count.set(StepStorage.getSteps().size());
            latch.countDown();
        });

        thread1.start();
        thread2.start();
        latch.await();

        // Each thread should see only its own steps
        assertEquals(2, thread1Count.get());
        assertEquals(1, thread2Count.get());

        // Main thread should have empty storage
        assertTrue(StepStorage.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Should handle adding null step gracefully")
    void testAddNullStep() {
        StepStorage.addStep(null);

        List<TestStep> steps = StepStorage.getSteps();
        assertEquals(1, steps.size());
        assertEquals(null, steps.get(0));
    }

    @Test
    @DisplayName("Should handle large number of steps")
    void testLargeNumberOfSteps() {
        int stepCount = 1000;

        for (int i = 0; i < stepCount; i++) {
            TestStep step = new TestStep();
            step.setStepTitle("Step " + i);
            step.setDuration(i * 10.5);
            StepStorage.addStep(step);
        }

        List<TestStep> steps = StepStorage.getSteps();

        assertEquals(stepCount, steps.size());
        assertEquals("Step 0", steps.get(0).getStepTitle());
        assertEquals("Step 999", steps.get(999).getStepTitle());
    }
}
