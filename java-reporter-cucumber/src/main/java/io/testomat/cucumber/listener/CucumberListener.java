package io.testomat.cucumber.listener;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.artifact.client.AwsService;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.exception.CucumberListenerException;
import io.testomat.cucumber.extractor.TestDataExtractor;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener implements Plugin, EventListener {
    private final GlobalRunManager runManager;
    private final CucumberTestResultConstructor resultConstructor;
    private final AwsService awsService;
    private final TestDataExtractor dataExtractor;

    /**
     * Creates a new listener with default dependencies.
     */
    public CucumberListener() {
        this.dataExtractor = new TestDataExtractor();
        this.runManager = GlobalRunManager.getInstance();
        this.resultConstructor = new CucumberTestResultConstructor();
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
                            AwsService awsService, TestDataExtractor dataExtractor) {
        this.runManager = runManager;
        this.resultConstructor = resultConstructor;
        this.awsService = awsService;
        this.dataExtractor = dataExtractor;
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
        runManager.incrementSuiteCounter();
        onTestRunStartedHook(event);
    }

    void handleTestRunFinished(TestRunFinished event) {
        runManager.decrementSuiteCounter();
        onTestRunFinishedHook(event);
    }

    void handleTestCaseFinished(TestCaseFinished event) {
        if (!runManager.isActive()) {
            return;
        }

        if (event == null) {
            throw new CucumberListenerException("The listener received null event");
        }

        try {
            TestResult result = resultConstructor.constructTestRunResult(event);
            runManager.reportTest(result);
            onTestCaseFinishedHook(event);
        } catch (Exception e) {
            String testName = event.getTestCase() != null ? event.getTestCase().getName()
                    : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        } finally {
            afterEach(event);
        }
    }

    /**
     * Called after each test case execution, similar to JUnit/TestNG afterEach.
     * Override this method to add custom post-test logic.
     *
     * @param event the test case finished event
     */
    protected void afterEach(TestCaseFinished event) {
        awsService.uploadAllArtifactsForTest(dataExtractor.extractTitle(event),
                event.getTestCase().getId().toString(),
                dataExtractor.extractTestId(event));
        afterEachHook(event);
    }

    /**
     * Hook called when test run starts. Override to add custom logic.
     *
     * @param event the test run started event
     */
    protected void onTestRunStartedHook(TestRunStarted event) {
    }

    /**
     * Hook called when test run finishes. Override to add custom logic.
     *
     * @param event the test run finished event
     */
    protected void onTestRunFinishedHook(TestRunFinished event) {
    }

    /**
     * Hook called when test case finishes. Override to add custom logic.
     *
     * @param event the test case finished event
     */
    protected void onTestCaseFinishedHook(TestCaseFinished event) {
    }

    /**
     * Hook called after each test case execution. Override to add custom logic.
     *
     * @param event the test case finished event
     */
    protected void afterEachHook(TestCaseFinished event) {
    }
}
