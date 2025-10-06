package io.testomat.core.step;

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
        long startTime = System.currentTimeMillis();

        log.info("Step aspect triggered for: {}", stepName);

        try {
            return executeStepSuccessfully(joinPoint, stepName, startTime);
        } catch (Throwable e) {
            handleStepFailure(stepName, startTime, e);
            throw e;
        }
    }

    private Object executeStepSuccessfully(ProceedingJoinPoint joinPoint, String stepName, long startTime) throws Throwable {
        Object result = joinPoint.proceed();
        long duration = calculateDuration(startTime);

        recordStep(stepName, duration);
        log.info("Step '{}' added to storage. Total steps: {}", stepName, StepStorage.getSteps().size());

        return result;
    }

    private void handleStepFailure(String stepName, long startTime, Throwable e) {
        long duration = calculateDuration(startTime);
        log.error("Step '{}' failed after {} ms", stepName, duration, e);
        recordStep(stepName, duration);
    }

    private long calculateDuration(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private void recordStep(String stepName, long duration) {
        TestStep testStep = createTestStep(stepName, duration);
        StepStorage.addStep(testStep);
    }

    /**
     * Resolves the step name from the annotation value or method name.
     * Supports parameter substitution using {parameterName} placeholders.
     *
     * @param joinPoint the join point representing the intercepted method
     * @param step      the Step annotation instance
     * @return resolved step name with substituted parameters
     */
    private String resolveStepName(ProceedingJoinPoint joinPoint, Step step) {
        String stepName = getStepNameTemplate(joinPoint, step);
        return substituteParameters(stepName, joinPoint);
    }

    private String getStepNameTemplate(ProceedingJoinPoint joinPoint, Step step) {
        if (step.value() != null && !step.value().isEmpty()) {
            return step.value();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getName();
    }

    /**
     * Substitutes parameter placeholders in the step name with actual parameter values.
     * Supports both indexed placeholders {0}, {1}, etc. and named placeholders {parameterName}.
     * Indexed placeholders work in all cases, while named placeholders require compilation
     * with -parameters flag or debug information.
     *
     * @param stepName  the step name template with placeholders
     * @param joinPoint the join point containing method parameters
     * @return step name with substituted parameter values
     */
    private String substituteParameters(String stepName, ProceedingJoinPoint joinPoint) {
        Object[] parameterValues = joinPoint.getArgs();

        if (parameterValues == null || parameterValues.length == 0) {
            return stepName;
        }

        String result = substituteIndexedPlaceholders(stepName, parameterValues);
        result = substituteNamedPlaceholders(result, joinPoint, parameterValues);

        return result;
    }

    private String substituteIndexedPlaceholders(String stepName, Object[] parameterValues) {
        String result = stepName;
        for (int i = 0; i < parameterValues.length; i++) {
            String placeholder = "{" + i + "}";
            String value = formatParameterValue(parameterValues[i]);
            result = result.replace(placeholder, value);
        }
        return result;
    }

    private String substituteNamedPlaceholders(String stepName, ProceedingJoinPoint joinPoint, Object[] parameterValues) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        if (parameterNames == null || parameterNames.length != parameterValues.length) {
            return stepName;
        }

        String result = stepName;
        for (int i = 0; i < parameterNames.length; i++) {
            String placeholder = "{" + parameterNames[i] + "}";
            String value = formatParameterValue(parameterValues[i]);
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * Formats a parameter value for display in step name.
     *
     * @param value the parameter value
     * @return formatted string representation
     */
    private String formatParameterValue(Object value) {
        if (value == null) {
            return "null";
        }
        return value.toString();
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
