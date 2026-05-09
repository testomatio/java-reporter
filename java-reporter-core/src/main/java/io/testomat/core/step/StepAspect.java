package io.testomat.core.step;

import static io.testomat.core.facade.Testomatio.stepArtifact;

import io.testomat.core.annotation.Step;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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

    @Pointcut("execution(* *(..)) && @annotation(io.testomat.core.annotation.Step)")
    public void stepAnnotation() {}

    /**
     * Initializes and starts a test step before execution of a method
     * annotated with {@link Step}.
     *
     * <p>Creates a new {@link TestStep}, resolves the step name with
     * substituted parameters, starts step lifecycle tracking,
     * and records execution start time.
     *
     * @param joinPoint intercepted method invocation
     */
    @Before("stepAnnotation()")
    public void beforeStep(JoinPoint joinPoint) {
        Step step = resolveStepAnnotation(joinPoint);
        String stepName = resolveStepName(joinPoint, step);

        TestStep testStep = new TestStep();
        testStep.setStepTitle(stepName);
        testStep.setCategory("user");

        String stepId = testStep.getId().toString();

        StepLifecycle.start(testStep);
        StepTimer.start(stepId);

        log.debug("Step started: {}", stepName);
    }

    /**
     * Finalizes a test step after successful execution of a method
     * annotated with {@link Step}.
     *
     * <p>Marks the current step as passed, records execution duration,
     * attaches configured artifacts, and completes the step lifecycle.
     *
     * @param joinPoint intercepted method invocation
     */
    @AfterReturning("stepAnnotation()")
    public void afterSuccess(JoinPoint joinPoint) {
        Step step = resolveStepAnnotation(joinPoint);
        TestStep testStep = StepLifecycle.current();

        if (testStep == null) {
            log.warn("StepLifecycle.current() is null in afterSuccess");
            return;
        }

        long duration = calculateDuration(testStep.getId().toString());
        String stepName = resolveStepName(joinPoint, step);
        String[] artifacts = resolveAttachments(step);

        testStep.setStatus(StepStatus.passed);
        testStep.setDuration(duration);

        if (artifacts != null) {
            stepArtifact(artifacts);
        }

        log.debug("Step '{}' passed in {} ms", stepName, duration);

        StepLifecycle.finish();
    }

    /**
     * Finalizes a test step after failed execution of a method
     * annotated with {@link Step}.
     *
     * <p>Marks the current step as failed, records execution duration,
     * stores error details and stack trace, attaches configured artifacts,
     * and completes the step lifecycle.
     *
     * @param joinPoint intercepted method invocation
     * @param e thrown exception
     */
    @AfterThrowing(pointcut = "stepAnnotation()", throwing = "e")
    public void afterFailure(JoinPoint joinPoint, Throwable e) {
        Step step = resolveStepAnnotation(joinPoint);
        TestStep testStep = StepLifecycle.current();

        if (testStep == null) {
            log.warn("StepLifecycle.current() is null in afterFailure");
            return;
        }

        long duration = calculateDuration(testStep.getId().toString());
        String stepName = resolveStepName(joinPoint, step);
        String[] artifacts = resolveAttachments(step);

        testStep.setStatus(StepStatus.failed);
        testStep.setDuration(duration);
        testStep.setError(e.getMessage());
        testStep.setLog(Arrays.toString(e.getStackTrace()));

        if (artifacts != null) {
            stepArtifact(artifacts);
        }

        log.debug("Step '{}' failed in {} ms", stepName, duration, e);

        StepLifecycle.finish();
    }

    private long calculateDuration(String stepId) {
        return System.currentTimeMillis() - StepTimer.stop(stepId);
    }

    private Step resolveStepAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        Step step = method.getAnnotation(Step.class);
        if (step != null) {
            return step;
        }

        try {
            Method realMethod = joinPoint.getTarget()
                .getClass()
                .getMethod(method.getName(), method.getParameterTypes());

            return realMethod.getAnnotation(Step.class);

        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String resolveStepName(JoinPoint joinPoint, Step step) {
        String stepName = getStepNameTemplate(joinPoint, step);
        return substituteParameters(stepName, joinPoint);
    }

    private String getStepNameTemplate(JoinPoint joinPoint, Step step) {
        if (step != null && step.value() != null && !step.value().isEmpty()) {
            return step.value();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getName();
    }

    private String[] resolveAttachments(Step step) {
        if (step != null && step.artifacts() != null && step.artifacts().length > 0) {
            return step.artifacts();
        }
        return null;
    }

    /**
     * Substitutes parameter placeholders in the step name with actual argument values.
     * Supports both indexed placeholders ({0}, {1}, etc.) and named placeholders
     * ({parameterName}).
     *
     * <p>Indexed placeholders always work, while named placeholders require
     * compilation with the {@code -parameters} flag or debug information.
     *
     * @param stepName the step name template containing placeholders
     * @param joinPoint the join point containing method arguments
     * @return formatted step name with substituted argument values
     */
    private String substituteParameters(String stepName, JoinPoint joinPoint) {
        Object[] parameterValues = joinPoint.getArgs();

        if (parameterValues == null || parameterValues.length == 0) {
            return stepName;
        }

        String result = stepName;

        for (int i = 0; i < parameterValues.length; i++) {
            String placeholder = "{" + i + "}";
            String value = format(parameterValues[i]);
            result = result.replace(placeholder, value);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        if (parameterNames != null && parameterNames.length == parameterValues.length) {
            for (int i = 0; i < parameterNames.length; i++) {
                String placeholder = "{" + parameterNames[i] + "}";
                String value = format(parameterValues[i]);
                result = result.replace(placeholder, value);
            }
        }

        return result;
    }

    private String format(Object value) {
        return value == null ? "null" : value.toString();
    }
}