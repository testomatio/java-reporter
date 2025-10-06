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
     * Captures the step name and execution duration, then creates a {@link TestStep} object
     * and stores it in ThreadLocal storage for later inclusion in test reports.
     *
     * @param joinPoint the join point representing the intercepted method
     * @param step      the Step annotation instance
     * @return the result of the intercepted method execution
     * @throws Throwable if the underlying method execution fails
     */
    @Around("execution(@io.testomat.core.annotation.Step * *(..)) && @annotation(step)")
    public Object aroundStep(ProceedingJoinPoint joinPoint, Step step) throws Throwable {
        String stepName = resolveStepName(joinPoint, step);
        long startMillis = System.currentTimeMillis();

        log.info("Step aspect triggered for: {}", stepName);

        try {
            Object result = joinPoint.proceed();
            long durationMillis = System.currentTimeMillis() - startMillis;

            TestStep testStep = createTestStep(stepName, durationMillis);
            StepStorage.addStep(testStep);

            log.info("Step '{}' added to storage. Total steps: {}", stepName, StepStorage.getSteps().size());

            return result;
        } catch (Throwable e) {
            long durationMillis = System.currentTimeMillis() - startMillis;
            log.error("Step '{}' failed after {} ms", stepName, durationMillis, e);

            TestStep testStep = createTestStep(stepName, durationMillis);
            StepStorage.addStep(testStep);

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
     * @param stepName       the name of the step
     * @param durationMillis the execution duration in milliseconds
     * @return populated TestStep object
     */
    private TestStep createTestStep(String stepName, long durationMillis) {
        TestStep testStep = new TestStep();
        testStep.setCategory("user");
        testStep.setStepTitle(stepName);
        testStep.setDuration(durationMillis);

        log.debug("Step '{}' completed in {} ms", stepName, durationMillis);

        return testStep;
    }
}
