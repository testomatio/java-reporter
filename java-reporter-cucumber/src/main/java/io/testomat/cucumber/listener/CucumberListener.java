package io.testomat.cucumber.listener;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.testomat.core.exception.ReportTestResultException;
import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.constructor.TestResultWrapper;
import io.testomat.cucumber.extractor.CucumberMetaDataExtractor;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener implements Plugin, EventListener {
    private final GlobalRunManager runManager = GlobalRunManager.getInstance();
    private final CucumberMetaDataExtractor metaDataExtractor = new CucumberMetaDataExtractor();
    private final CucumberTestResultConstructor resultConstructor =
            new CucumberTestResultConstructor();

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class,
                e -> runManager.incrementSuiteCounter());
        eventPublisher.registerHandlerFor(TestRunFinished.class,
                e -> runManager.decrementSuiteCounter());
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    void handleTestCaseFinished(TestCaseFinished event) {
        if (!runManager.isActive()) {
            return;
        }

        try {
            String status = normalizeStatus(
                    event.getResult() != null
                            ? event.getResult().getStatus()
                            : null);
            TestMetadata metadata = metaDataExtractor.extractTestMetadata(event.getTestCase());

            TestResultWrapper wrapper = TestResultWrapper.builder()
                    .withTestMetadata(metadata)
                    .withStatus(status)
                    .withCucumberTestCaseFinished(event)
                    .build();

            TestResult result = resultConstructor.constructTestRunResult(wrapper);
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = event.getTestCase() != null ? event.getTestCase().getName()
                    : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    private String normalizeStatus(Object frameworkStatus) {
        if (frameworkStatus == null) {
            return FAILED;
        }

        switch (frameworkStatus.toString().toUpperCase()) {
            case "PASSED":
            case "SUCCESS":
            case "SUCCESSFUL":
                return PASSED;
            case "SKIPPED":
            case "PENDING":
            case "UNDEFINED":
            case "AMBIGUOUS":
            case "DISABLED":
            case "ABORTED":
                return SKIPPED;
            default:
                return FAILED;
        }
    }
}
