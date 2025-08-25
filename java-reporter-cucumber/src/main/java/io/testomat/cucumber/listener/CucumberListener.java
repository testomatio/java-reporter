package io.testomat.cucumber.listener;

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
import io.testomat.cucumber.listener.normalizer.StatusNormalizer;
import java.util.List;

/**
 * Cucumber plugin for Testomat.io integration.
 * Reports Cucumber test execution results to Testomat.io platform.
 */
public class CucumberListener implements Plugin, EventListener {
    private final GlobalRunManager runManager;
    private final CucumberMetaDataExtractor metaDataExtractor;
    private final CucumberTestResultConstructor resultConstructor;
    private final StatusNormalizer statusNormalizer;

    public CucumberListener() {
        this.runManager = GlobalRunManager.getInstance();
        this.metaDataExtractor = new CucumberMetaDataExtractor();
        this.resultConstructor = new CucumberTestResultConstructor();
        this.statusNormalizer = new StatusNormalizer();
    }

    /**
     * Testing constructor
     */
    public CucumberListener(CucumberTestResultConstructor resultConstructor,
                            CucumberMetaDataExtractor metaDataExtractor,
                            StatusNormalizer statusNormalizer,
                            GlobalRunManager runManager) {
        this.runManager = runManager;
        this.metaDataExtractor = metaDataExtractor;
        this.resultConstructor = resultConstructor;
        this.statusNormalizer = statusNormalizer;
    }

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
            String status = statusNormalizer.normalizeStatus(event.getResult() != null
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
}
