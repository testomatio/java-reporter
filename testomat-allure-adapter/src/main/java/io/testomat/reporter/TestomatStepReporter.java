package io.testomat.reporter;

import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StepResult;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.TestStep;

/**
 * Step lifecycle listener that reports Allure steps to Testomat.
 */
public class TestomatStepReporter implements StepLifecycleListener {

    /** Starts Testomat step when Allure step starts. */
    @Override
    public void afterStepStart(StepResult result) {
        StepLifecycle.start(new TestStep());
    }

    /** Finalizes Testomat step when Allure step finishes. */
    @Override
    public void afterStepStop(StepResult result) {
        TestStep testStep = StepLifecycle.current();
        testStep.setCategory("user");
        testStep.setStepTitle(result.getName());

        long durationMillis = result.getStop() - result.getStart();
        testStep.setDuration(durationMillis);

        switch (result.getStatus()) {
            case PASSED:
                testStep.setStatus(StepStatus.passed);
                break;
            case FAILED:
            case BROKEN:
                testStep.setStatus(StepStatus.failed);
                testStep.setError(result.getStatusDetails().getMessage());
                testStep.setLog(result.getStatusDetails().getTrace());
                break;
            default:
                testStep.setStatus(StepStatus.none);
        }

        StepLifecycle.finish();
    }
}
