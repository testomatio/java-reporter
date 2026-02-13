package io.testomat.karate.hooks;

import static java.util.Objects.isNull;

import com.intuit.karate.RuntimeHook;
import com.intuit.karate.Suite;
import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.karate.constructor.KarateTestResultConstructor;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.karate.exception.KarateHookException;
import io.testomat.karate.extractor.TestDataExtractor;

/**
 * Karate hook for Testomat.io integration. Reports Karate test execution results to Testomat.io platform.
 */
public class KarateHook implements RuntimeHook {

    private final KarateTestResultConstructor resultConstructor;
    private final TestDataExtractor dataExtractor;
    private final FacadeFunctionsHandler functionsHandler;
    private final GlobalRunManager runManager;
    private final AwsService awsService;

    /**
     * Creates a new hook with default dependencies.
     */
    public KarateHook() {
        this.functionsHandler = new FacadeFunctionsHandler();
        this.resultConstructor = new KarateTestResultConstructor();
        this.runManager = GlobalRunManager.getInstance();
        this.dataExtractor = new TestDataExtractor();
        this.awsService = new AwsService();
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
            throw new ReportTestResultException(String.format("Failed to report test result for: %s", testName), e);
        } finally {
            afterEach(sr);
        }
    }

    @Override
    public void afterSuite(Suite suite) {
        runManager.decrementSuiteCounter();
    }

    /**
     * Called after each test case execution, similar to JUnit/TestNG afterEach.
     * Override this method to add custom post-test logic.
     *
     * @param sr the ScenarioRuntime
     */
    protected void afterEach(ScenarioRuntime sr) {
        functionsHandler.handleFacadeFunctions(sr);
    }

}
