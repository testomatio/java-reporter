package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.constructor.CucumberTestResultConstructor;
import com.testomatio.reporter.core.constructor.ResultConstructor;
import com.testomatio.reporter.core.constructor.TestCaseResultWrapper;
import com.testomatio.reporter.core.extractor.CucumberMetaDataExtractor;
import com.testomatio.reporter.core.extractor.MetaDataExtractor;
import com.testomatio.reporter.model.TestMetadata;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener extends AbstractTestFrameworkListener implements Plugin, EventListener {
    private final MetaDataExtractor<TestCase> metaDataExtractor = new CucumberMetaDataExtractor();

    /**
     * Creates new Cucumber listener.
     */
    public CucumberListener() {
        super();
    }

    /**
     * Creates new Cucumber listener with output configuration.
     *
     * @param out output configuration parameter
     */
    public CucumberListener(String out) {
        super();
        LOGGER.fine("CucumberListener initialized with output: " + out);
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new CucumberTestResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof TestCaseFinished) {
            builder.withCucumberTestCaseFinished((TestCaseFinished) frameworkSpecificData);
        }
    }

    /**
     * Registers event handlers with Cucumber event publisher.
     *
     * @param eventPublisher Cucumber event publisher
     */
    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    /**
     * Handles Cucumber test run start event.
     *
     * @param event test run started event
     */
    private void handleTestRunStarted(TestRunStarted event) {
        handleSuiteStarted("Cucumber Test Run");
    }

    /**
     * Handles Cucumber test run finish event.
     *
     * @param event test run finished event
     */
    private void handleTestRunFinished(TestRunFinished event) {
        handleSuiteFinished("Cucumber Test Run");
    }

    /**
     * Handles individual test case start event.
     *
     * @param event test case started event
     */
    private void handleTestCaseStarted(TestCaseStarted event) {
        LOGGER.finer("Starting test case: " + event.getTestCase().getName());
    }

    /**
     * Handles individual test case finish event and reports result.
     *
     * @param event test case finished event
     */
    private void handleTestCaseFinished(TestCaseFinished event) {
        String status = determineTestStatus(event);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(event.getTestCase());

        logMetadataCreation(metadata);
        reportTestResult(metadata, status, event);
    }

    /**
     * Determines test status from Cucumber test case finished event.
     *
     * @param event test case finished event
     * @return normalized test status
     */
    private String determineTestStatus(TestCaseFinished event) {
        if (event == null || event.getResult() == null || event.getResult().getStatus() == null) {
            return normalizeStatus(null);
        }
        return normalizeStatus(event.getResult().getStatus());
    }
}