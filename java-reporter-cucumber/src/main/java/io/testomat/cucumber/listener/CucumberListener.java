package io.testomat.cucumber.listener;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.*;
import io.testomat.core.exception.ReportTestResultException;

import io.testomat.core.model.TestMetadata;
import io.testomat.core.model.TestResult;
import io.testomat.core.runmanager.GlobalRunManager;
import io.testomat.cucumber.constructor.CucumberTestResultConstructor;
import io.testomat.cucumber.constructor.TestResultWrapper;
import io.testomat.cucumber.extractor.CucumberMetaDataExtractor;
import io.testomat.cucumber.listener.normalizer.StatusNormalizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    .withExample(getParams(event))
                    .build();

            TestResult result = resultConstructor.constructTestRunResult(wrapper);
            runManager.reportTest(result);

        } catch (Exception e) {
            String testName = event.getTestCase() != null ? event.getTestCase().getName()
                    : "Unknown Test";
            throw new ReportTestResultException("Failed to report test result for: " + testName, e);
        }
    }

    private Map<Object, Object> getParams(TestCaseFinished event) {

        Map<Object, Object> params = new HashMap<>();

        List<TestStep> testSteps = event.getTestCase().getTestSteps();

        for (TestStep testStep : testSteps) {
            if (testStep instanceof PickleStepTestStep) {
                String stepText = ((PickleStepTestStep) testStep).getStepText();
                params.putAll(extractValuesFromStepText(stepText));
            }
        }

        return params;
    }

    private Map<String, Object> extractValuesFromStepText(String stepText) {
        Map<String, Object> values = new HashMap<>();

        Pattern quotedPattern = Pattern.compile("[\"']([^\"']+)[\"']|\\b(\\d+(?:\\.\\d+)?)\\b");
        Matcher matcher = quotedPattern.matcher(stepText);

        int index = 0;
        while (matcher.find()) {
            Object value;
            if (matcher.group(1) != null) {
                value = matcher.group(1);
            } else {
                String numStr = matcher.group(2);
                if (numStr.contains(".")) {
                    value = Double.parseDouble(numStr);
                } else {
                    value = Long.parseLong(numStr);
                }
            }
            values.put("step_value_" + index, value);
            index++;
        }

        return values;
    }
}
