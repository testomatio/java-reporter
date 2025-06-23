package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.constructor.CucumberTestCaseResultConstructor;
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
 * Cucumber plugin that integrates test execution with Testomat.io reporting system.
 * Extends AbstractTestFrameworkListener to leverage common reporting functionality.
 */
public class CucumberListener extends AbstractTestFrameworkListener implements Plugin, EventListener {
    private final MetaDataExtractor<TestCase> metaDataExtractor = new CucumberMetaDataExtractor();


    public CucumberListener() {
        super();
    }

    public CucumberListener(String out) {
        super();
        LOGGER.fine("CucumberListener initialized with output: " + out);
    }

    @Override
    protected ResultConstructor createResultConstructor() {
        return new CucumberTestCaseResultConstructor();
    }

    @Override
    protected void addFrameworkSpecificData(TestCaseResultWrapper.Builder builder, Object frameworkSpecificData) {
        if (frameworkSpecificData instanceof TestCaseFinished) {
            builder.withCucumberTestCaseFinished((TestCaseFinished) frameworkSpecificData);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        handleSuiteStarted("Cucumber Test Run");
    }

    private void handleTestRunFinished(TestRunFinished event) {
        handleSuiteFinished("Cucumber Test Run");
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        LOGGER.finer("Starting test case: " + event.getTestCase().getName());
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        String status = determineTestStatus(event);
        TestMetadata metadata = metaDataExtractor.extractTestMetadata(event.getTestCase());

        logMetadataCreation(metadata);
        reportTestResult(metadata, status, event);
    }

    private String determineTestStatus(TestCaseFinished event) {
        if (event == null || event.getResult() == null || event.getResult().getStatus() == null) {
            return normalizeStatus(null);
        }
        return normalizeStatus(event.getResult().getStatus());
    }
}