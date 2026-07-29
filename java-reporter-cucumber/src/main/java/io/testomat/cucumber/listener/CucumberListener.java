package io.testomat.cucumber.listener;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.agent.TestomatAgent;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.exception.CucumberListenerException;
import io.testomat.cucumber.extractor.TestDataExtractor;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener extends AbstractHooksContainer
        implements Plugin, EventListener {
    private final CucumberTestResultConstructor resultConstructor;
    private final FacadeFunctionsHandler functionsHandler;
    private final TestDataExtractor dataExtractor;
    private final GlobalRunManager runManager;
    private final AwsService awsService;

    /**
     * Creates a new listener with default dependencies.
     */
    public CucumberListener() {
        TestomatAgent.install();
        this.resultConstructor = new CucumberTestResultConstructor();
        this.functionsHandler = new FacadeFunctionsHandler();
        this.runManager = GlobalRunManager.getInstance();
        this.dataExtractor = new TestDataExtractor();
        this.awsService = new AwsService();
    }

    /**
     * Creates a new listener with specified dependencies.
     * Used primarily for testing with mocked dependencies.
     *
     * @param resultConstructor the test result constructor
     * @param runManager        the global run manager
     */
    public CucumberListener(CucumberTestResultConstructor resultConstructor,
                            GlobalRunManager runManager,
                            AwsService awsService,
                            TestDataExtractor dataExtractor,
                            FacadeFunctionsHandler functionsHandler) {
        this.resultConstructor = resultConstructor;
        this.functionsHandler = functionsHandler;
        this.dataExtractor = dataExtractor;
        this.runManager = runManager;
        this.awsService = awsService;
    }

    /**
     * Registers event handlers for Cucumber test execution events.
     *
     * @param eventPublisher the Cucumber event publisher
     */
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(
                TestRunStarted.class, this::handleTestRunStarted);
        eventPublisher.registerHandlerFor(
                TestRunFinished.class, this::handleTestRunFinished);
        eventPublisher.registerHandlerFor(
                TestCaseFinished.class, this::handleTestCaseFinished);
    }

    void handleTestRunStarted(TestRunStarted event) {
        onTestRunStartedHookBeforeExecution(event);
        runManager.incrementSuiteCounter();
        onTestRunStartedHookAfterExecution(event);
    }

    void handleTestRunFinished(TestRunFinished event) {
        onTestRunFinishedHookBeforeExecution(event);
        runManager.decrementSuiteCounter();
        onTestRunFinishedHookAfterExecution(event);
    }

    void handleTestCaseFinished(TestCaseFinished event) {
        if (!runManager.isActive()) {
            return;
        }

        if (event == null) {
            throw new CucumberListenerException("The listener received null event");
        }

        try {
            onTestCaseFinishedHookBeforeExecution(event);
            TestResult result = resultConstructor.constructTestRunResult(event);
            runManager.reportTest(result);
            onTestCaseFinishedHookAfterExecution(event);
        } catch (Exception e) {
            String testName = event.getTestCase() != null ? event.getTestCase().getName()
                    : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        } finally {
            afterEach(event);
            onTestCaseFinishedHookFinally(event);
        }
    }

    /**
     * Called after each test case execution, similar to JUnit/TestNG afterEach.
     * Override this method to add custom post-test logic.
     *
     * @param event the test case finished event
     */
    protected void afterEach(TestCaseFinished event) {
        afterEachHookBeforeExecution(event);
        functionsHandler.handleFacadeFunctions(event);
        afterEachHookAfterExecution(event);
    }
}
