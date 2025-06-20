package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;

public class CucumberListener extends BaseTestReporter implements Plugin, EventListener {

    public CucumberListener() {
        LOGGER.fine("CucumberListener initialized");
    }

    public CucumberListener(String out) {
        LOGGER.fine("CucumberListener initialized with output: " + out);
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        handleSuiteStart("Cucumber test run");
    }

    private void handleTestRunFinished(TestRunFinished event) {
        handleSuiteFinish("Cucumber test run");
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        LOGGER.finer("Starting test case: " + event.getTestCase().getName());
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        String testCaseName = event.getTestCase().getName();
        String status = determineTestStatus(event);
        TestMetadata metadata = metadataExtractor.extractFromCucumber(event.getTestCase());
        
        Throwable error = event.getResult().getError();
        reportTest(testCaseName, metadata, status, error);
    }

    private String determineTestStatus(TestCaseFinished event) {
        if (event == null || event.getResult() == null || event.getResult().getStatus() == null) {
            return FAILED;
        }

        switch (event.getResult().getStatus()) {
            case PASSED:
                return PASSED;
            case SKIPPED:
            case PENDING:
            case UNDEFINED:
            case AMBIGUOUS:
                return SKIPPED;
            default:
                return FAILED;
        }
    }
}