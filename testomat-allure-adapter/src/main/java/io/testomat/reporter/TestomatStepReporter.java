package io.testomat.reporter;

import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.testomat.agent.AllureAgent;
import io.testomat.core.step.StepLifecycle;
import io.testomat.core.step.StepStatus;
import io.testomat.core.step.TestStep;
import java.util.Objects;

/**
 * Step lifecycle listener that reports Allure steps to Testomat.
 */
public class TestomatStepReporter implements StepLifecycleListener {

    static {
        AllureAgent.install();
    }

    /** Starts Testomat step when Allure step starts. */
    @Override
    public void afterStepStart(StepResult result) {
        StepLifecycle.start(new TestStep());
    }

    /** Finalizes Testomat step when Allure step finishes. */
    @Override
    public void afterStepStop(StepResult result) {
        TestStep testStep = StepLifecycle.current();

        if (testStep == null) {
            return;
        }

        testStep.setCategory("user");
        testStep.setStepTitle(Objects.requireNonNullElse(result.getName(), ""));

        Long start = result.getStart();
        Long stop = result.getStop();
        long duration = (start != null && stop != null) ? (stop - start) : 0L;

        testStep.setDuration(duration);

        switch (result.getStatus()) {
            case PASSED:
                testStep.setStatus(StepStatus.passed);
                break;
            case FAILED:
            case BROKEN:
                testStep.setStatus(StepStatus.failed);

                StatusDetails details = result.getStatusDetails();
                if (details != null) {
                    if (details.getMessage() != null) {
                        testStep.setError(details.getMessage());
                    }
                    if (details.getTrace() != null) {
                        testStep.setLog(details.getTrace());
                    }
                }
                break;
            default:
                testStep.setStatus(StepStatus.none);
        }

        StepLifecycle.finish();
    }
}
