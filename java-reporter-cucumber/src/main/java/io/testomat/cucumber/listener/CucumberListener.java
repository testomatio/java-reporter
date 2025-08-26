package io.testomat.cucumber.listener;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.exception.CucumberListenerException;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener implements Plugin, EventListener {
    private final GlobalRunManager runManager;
    private final CucumberTestResultConstructor resultConstructor;

    public CucumberListener() {
        this.runManager = GlobalRunManager.getInstance();
        this.resultConstructor = new CucumberTestResultConstructor();
    }

    /**
     * Testing constructor
     */
    public CucumberListener(CucumberTestResultConstructor resultConstructor,
                            GlobalRunManager runManager) {
        this.runManager = runManager;
        this.resultConstructor = resultConstructor;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(
                TestRunStarted.class, e -> runManager.incrementSuiteCounter());
        eventPublisher.registerHandlerFor(
                TestRunFinished.class, e -> runManager.decrementSuiteCounter());
        eventPublisher.registerHandlerFor(
                TestCaseFinished.class, this::handleTestCaseFinished);
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
        } catch (Exception e) {
            String testName = event.getTestCase() != null ? event.getTestCase().getName()
                    : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }
}
