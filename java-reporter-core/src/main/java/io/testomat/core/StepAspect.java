package io.testomat.core;

import io.testomat.core.annotation.Step;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AspectJ aspect that intercepts methods annotated with {@link Step} to capture step execution metadata.
 * This aspect records step name and execution duration for test reporting purposes.
 */
@Aspect
public class StepAspect {
    private static final Logger log = LoggerFactory.getLogger(StepAspect.class);
    private static final long NANOS_IN_MILLISECOND = 1_000_000L;

    /**
     * Intercepts method execution for methods annotated with {@link Step}.
     * Captures the step name and execution duration, then creates a {@link TestStep} object.
     *
     * @param joinPoint the join point representing the intercepted method
     * @param step      the Step annotation instance
     * @return TestStep object containing step metadata
     * @throws Throwable if the underlying method execution fails
     */
    @Around("@annotation(step)")
    public TestStep aroundStep(ProceedingJoinPoint joinPoint, Step step) throws Throwable {
        String stepName = resolveStepName(joinPoint, step);
        long startNanos = System.nanoTime();

        try {
            joinPoint.proceed();
            long durationNanos = System.nanoTime() - startNanos;

            return createTestStep(stepName, durationNanos);
        } catch (Throwable e) {
            long durationNanos = System.nanoTime() - startNanos;
            log.error("Step '{}' failed after {} ms", stepName, durationNanos / NANOS_IN_MILLISECOND, e);
            throw e;
        }
    }

    /**
     * Resolves the step name from the annotation value or method name.
     *
     * @param joinPoint the join point representing the intercepted method
     * @param step      the Step annotation instance
     * @return resolved step name
     */
    private String resolveStepName(ProceedingJoinPoint joinPoint, Step step) {
        if (step.value() != null && !step.value().isEmpty()) {
            return step.value();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getName();
    }

    /**
     * Creates a TestStep object with the provided metadata.
     *
     * @param stepName      the name of the step
     * @param durationNanos the execution duration in nanoseconds
     * @return populated TestStep object
     */
    private TestStep createTestStep(String stepName, long durationNanos) {
        TestStep testStep = new TestStep();
        testStep.setStepTitle(stepName);
        testStep.setDuration(durationNanos);

        log.debug("Step '{}' completed in {} ms", stepName, durationNanos / NANOS_IN_MILLISECOND);

        return testStep;
    }
}
