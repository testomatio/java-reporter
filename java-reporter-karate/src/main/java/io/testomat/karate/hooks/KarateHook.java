package io.testomat.karate.hooks;

import static java.util.Objects.isNull;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.Suite;
import com.intuit.karate.core.ScenarioRuntime;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.core.step.StepStorage;
import io.testomat.core.step.StepTimer;
import io.testomat.core.step.TestStep;
import io.testomat.karate.adapter.CustomKarateEngineAdapter;
import io.testomat.karate.adapter.KarateEngineAdapter;
import io.testomat.karate.constructor.KarateTestResultConstructor;
import io.testomat.karate.exception.KarateHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime hook for integrating Karate test execution with Testomat.io.
 * Reports Karate test execution results to the
 * Testomat.io platform.
 */
public class KarateHook implements RuntimeHook {

    private static final Logger log = LoggerFactory.getLogger(KarateHook.class);

    private static final String LOG_NEXT_STEP = "log_next_step";
    private static final String LOG_NEXT_STEP_TITLE = "log_next_step_title";
    private static final String LOG_STEPS = "LogSteps";

    private final KarateTestResultConstructor resultConstructor;
    private final FacadeFunctionsHandler functionsHandler;
    private final GlobalRunManager runManager;
    private final KarateEngineAdapter engine;

    public KarateHook(
            KarateTestResultConstructor resultConstructor,
            FacadeFunctionsHandler functionsHandler,
            GlobalRunManager runManager,
            KarateEngineAdapter engine
    ) {
        this.resultConstructor = resultConstructor;
        this.functionsHandler = functionsHandler;
        this.runManager = runManager;
        this.engine = engine;
    }

    /**
     * Creates a new Karate hook with default dependencies.
     */

    public KarateHook() {
        this(
            new KarateTestResultConstructor(),
            new FacadeFunctionsHandler(),
                GlobalRunManager.getInstance(),
            new CustomKarateEngineAdapter()
        );
    }

    @Override
    public void beforeSuite(Suite suite) {
        runManager.incrementSuiteCounter();
    }

    @Override
    public boolean beforeStep(Step step, ScenarioRuntime sr) {
        boolean isMarked = Boolean.TRUE.equals(engine.getVariable(LOG_NEXT_STEP));

        boolean isDslStep = switch (step.getPrefix() == null ? "" : step.getPrefix()) {
            case "Given", "When", "Then", "And", "But" -> true;
            default -> false;
        };

        boolean logAllSteps = isDslStep && sr.tags.getTags().contains(LOG_STEPS);

        if (logAllSteps || isMarked) {
            engine.setVariable(LOG_NEXT_STEP, false);
            String stepId = Thread.currentThread().getId() + ":" + System.identityHashCode(step);
            StepTimer.start(stepId);
        }
        return true;
    }

    @Override
    public void afterStep(StepResult result, ScenarioRuntime sr) {
        Step step = result.getStep();
        String stepId = Thread.currentThread().getId() + ":" + System.identityHashCode(step);
        long durationMillis = StepTimer.stop(stepId);

        if (durationMillis != -1) {
            String stepName = step.getText();
            Object title = engine.getVariable(LOG_NEXT_STEP_TITLE);

            if (title != null) {
                stepName = title.toString();
                engine.setVariable(LOG_NEXT_STEP_TITLE, null);
            }

            TestStep testStep = new TestStep();
            testStep.setCategory("user");
            testStep.setStepTitle(stepName);
            testStep.setDuration(durationMillis);

            StepStorage.addStep(testStep);

            log.debug("Step '{}' completed in {} ms", stepName, durationMillis);
        }
    }

    @Override
    public void afterScenario(ScenarioRuntime sr) {
        if (!runManager.isActive()) {
            return;
        }

        if (isNull(sr)) {
            throw new KarateHookException("Karate hook the scenario runtime is null");
        }

        try {
            TestResult result = resultConstructor.constructTestRunResult(sr);
            runManager.reportTest(result);
        } catch (Exception e) {
            String testName = sr.scenario.getName();
            throw new ReportTestResultException(
                String.format("Failed to report test result for: %s", testName),
                e);
        } finally {
            afterEach(sr);
        }
    }

    @Override
    public void afterSuite(Suite suite) {
        runManager.decrementSuiteCounter();
    }

    /**
     * Called after each Karate test case execution, similar to
     * {@code @AfterEach} in JUnit or {@code @AfterMethod} in TestNG.
     * <p>
     * Override this method to add custom post-test logic.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished test case
     */
    protected void afterEach(ScenarioRuntime sr) {
        functionsHandler.handleFacadeFunctions(sr);
    }

}
