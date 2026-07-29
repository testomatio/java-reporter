package io.testomat.core.step;

import static io.testomat.core.facade.Testomatio.stepArtifact;

import io.testomat.core.annotation.Step;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepAdvice {

    public static final Logger log = LoggerFactory.getLogger(StepAdvice.class);

    @Advice.OnMethodEnter
    public static void enter(@Advice.Origin Method method, @Advice.AllArguments Object[] args) {
        Step step = method.getAnnotation(Step.class);
        if (step == null) return;

        String stepName = resolveStepName(method, step, args);

        TestStep testStep = new TestStep();
        testStep.setStepTitle(stepName);
        testStep.setCategory("user");

        StepLifecycle.start(testStep);
        StepTimer.start(testStep.getId().toString());

        log.debug("Step started: {}", stepName);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin Method method, @Advice.Thrown Throwable thrown) {
        Step step = method.getAnnotation(Step.class);
        if (step == null) return;

        TestStep testStep = StepLifecycle.current();
        if (testStep == null) {
            log.debug("StepLifecycle.current() is null in StepAdvice.exit");
            return;
        }

        long duration = StepTimer.stop(testStep.getId().toString());
        String stepName = testStep.getStepTitle();
        String[] artifacts = resolveAttachments(step);

        if (thrown != null) {
            testStep.setStatus(StepStatus.failed);
            testStep.setDuration(duration);
            testStep.setError(formatError(thrown));
            testStep.setLog(getStackTrace(thrown));

            log.debug("Step '{}' failed in {} ms", stepName, duration, thrown);
        } else {
            testStep.setStatus(StepStatus.passed);
            testStep.setDuration(duration);

            log.debug("Step '{}' passed in {} ms", stepName, duration);
        }

        if (artifacts != null) {
            stepArtifact(artifacts);
        }

        StepLifecycle.finish();
    }

    public static String resolveStepName(Method method, Step step, Object[] args) {
        String template = step.value();
        if (template == null || template.isEmpty()) {
            return method.getName();
        }
        return substituteParameters(template, args);
    }

    public static String substituteParameters(String stepName, Object[] parameterValues) {
        if (parameterValues == null || parameterValues.length == 0) {
            return stepName;
        }

        String result = stepName;
        for (int i = 0; i < parameterValues.length; i++) {
            result = result.replace("{" + i + "}", format(parameterValues[i]));
        }
        return result;
    }

    public static String format(Object value) {
        return value == null ? "null" : value.toString();
    }

    public static String[] resolveAttachments(Step step) {
        if (step != null && step.artifacts() != null && step.artifacts().length > 0) {
            return step.artifacts();
        }
        return null;
    }

    public static String formatError(Throwable t) {
        return t.getMessage() == null || t.getMessage().isBlank()
            ? t.getClass().getSimpleName()
            : t.getClass().getSimpleName() + ": " + t.getMessage();
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
