package io.testomat.karate.hooks;

import static java.util.Objects.isNull;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.Suite;
import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.karate.constructor.KarateTestResultConstructor;
import io.testomat.karate.exception.KarateHookException;

/**
 * Runtime hook for integrating Karate test execution with Testomat.io.
 * Reports Karate test execution results to the Testomat.io platform.
 */
public class KarateHook implements RuntimeHook {

    private final KarateTestResultConstructor resultConstructor;
    private final FacadeFunctionsHandler functionsHandler;
    private final GlobalRunManager runManager;

    public KarateHook(
            KarateTestResultConstructor resultConstructor,
            FacadeFunctionsHandler functionsHandler,
            GlobalRunManager runManager
    ) {
        this.resultConstructor = resultConstructor;
        this.functionsHandler = functionsHandler;
        this.runManager = runManager;
    }

    /**
     * Creates a new Karate hook with default dependencies.
     */

    public KarateHook() {
        this(
            new KarateTestResultConstructor(),
            new FacadeFunctionsHandler(),
                GlobalRunManager.getInstance()
        );
    }

    @Override
    public void beforeSuite(Suite suite) {
        runManager.incrementSuiteCounter();
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
