package com.testomatio.reporter.core.framework_integration;

import com.testomatio.reporter.core.GlobalRunManager;
import com.testomatio.reporter.exception.ReportTestResultException;
import com.testomatio.reporter.model.TestMetadata;
import com.testomatio.reporter.model.TestRunResult;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Supplier;

import static com.testomatio.reporter.constants.CommonConstants.FAILED;
import static com.testomatio.reporter.constants.CommonConstants.PASSED;
import static com.testomatio.reporter.constants.CommonConstants.SKIPPED;
import static com.testomatio.reporter.logger.LoggerUtils.getLogger;

public class CucumberListener implements Plugin, EventListener {

    private static final String UNKNOWN_SUITE = "Unknown Suite";
    private static final String UNKNOWN_FILE = "unknown.feature";
    private static final String UNKNOWN_TEST = "Unknown Test";
    private static final String TESTID_PREFIX = "@testid:";
    private static final String TITLE_PREFIX = "@title:";

    private final GlobalRunManager runManager = GlobalRunManager.getInstance();

    public CucumberListener() {
        getLogger(CucumberListener.class).fine("CucumberListener initialized");
    }

    public CucumberListener(String out) {
        getLogger(CucumberListener.class).fine("CucumberListener initialized with output: " + out);
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);
        eventPublisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        eventPublisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    private void handleTestRunStarted(TestRunStarted event) {
        getLogger(CucumberListener.class).fine("Cucumber test run started");
        runManager.incrementSuiteCounter();
    }

    private void handleTestRunFinished(TestRunFinished event) {
        getLogger(CucumberListener.class).fine("Cucumber test run finished");
        runManager.decrementSuiteCounter();
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        getLogger(CucumberListener.class).finer("Starting test case: " + event.getTestCase().getName());
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        if (!runManager.isActive()) {
            return;
        }

        String testCaseName = event.getTestCase().getName();
        try {
            String status = determineTestStatus(event);
            TestMetadata metadata = extractTestMetadata(event.getTestCase());
            TestRunResult result = createTestResult(metadata, status, event);

            reportTestResult(result, status);

        } catch (Exception e) {
            getLogger(CucumberListener.class).severe("Failed to report test result for: " + testCaseName);
            throw new ReportTestResultException("Failed to report test result for: " + testCaseName, e);
        }
    }

    private String determineTestStatus(TestCaseFinished event) {
        if (event == null || event.getResult() == null || event.getResult().getStatus() == null) {
            return FAILED;
        }

        switch (event.getResult().getStatus()) {
            case PASSED:
                return PASSED;
            case FAILED:
                return FAILED;
            case SKIPPED:
            case PENDING:
            case UNDEFINED:
            case AMBIGUOUS:
                return SKIPPED;
            default:
                return FAILED;
        }
    }

    private TestMetadata extractTestMetadata(io.cucumber.plugin.event.TestCase testCase) {
        String title = extractTitle(testCase);
        String testId = extractTestId(testCase);
        String suiteTitle = extractSuiteTitle(testCase);
        String file = extractFileName(testCase);

        return new TestMetadata(title, testId, suiteTitle, file);
    }

    private TestRunResult createTestResult(TestMetadata metadata, String status, TestCaseFinished event) {
        String message = null;
        String stack = null;

        Throwable error = event.getResult().getError();
        if (error != null) {
            message = error.getMessage();
            stack = getStackTrace(error);
        }

        return new TestRunResult(
                metadata.getTitle(),
                metadata.getTestId(),
                metadata.getSuiteTitle(),
                metadata.getFile(),
                status,
                message,
                stack
        );
    }

    private void reportTestResult(TestRunResult result, String status) {
        getLogger(CucumberListener.class).fine("Reporting test result: " + result.getTitle() + " - " + status);
        runManager.reportTest(result);
    }

    private String extractTitle(io.cucumber.plugin.event.TestCase testCase) {
        if (testCase == null) {
            return UNKNOWN_TEST;
        }

        Optional<String> titleFromTag = extractFromTags(testCase, TITLE_PREFIX)
                .map(tag -> tag.replace("_", " "));

        return titleFromTag.orElse(
                Optional.ofNullable(testCase.getName()).orElse(UNKNOWN_TEST)
        );
    }

    private String extractTestId(io.cucumber.plugin.event.TestCase testCase) {
        return extractFromTags(testCase, TESTID_PREFIX).orElse(null);
    }

    private Optional<String> extractFromTags(io.cucumber.plugin.event.TestCase testCase, String prefix) {
        if (testCase == null || testCase.getTags() == null) {
            return Optional.empty();
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.toLowerCase().startsWith(prefix))
                .map(tag -> tag.substring(prefix.length()))
                .findFirst();
    }

    private String extractSuiteTitle(io.cucumber.plugin.event.TestCase testCase) {
        return extractFileNameFromUri(testCase)
                .map(this::removeFileExtension)
                .orElse(UNKNOWN_SUITE);
    }

    private String extractFileName(io.cucumber.plugin.event.TestCase testCase) {
        return extractFileNameFromUri(testCase).orElse(UNKNOWN_FILE);
    }

    private Optional<String> extractFileNameFromUri(io.cucumber.plugin.event.TestCase testCase) {
        if (testCase == null || testCase.getUri() == null) {
            return Optional.empty();
        }

        return extractUriPath(testCase.getUri())
                .map(this::extractFileNameFromPath)
                .filter(name -> !name.isEmpty());
    }

    private Optional<String> extractUriPath(java.net.URI uri) {
        String path = safeGet(uri::getPath);
        if (path != null && !path.isEmpty()) {
            return Optional.of(cleanUriPath(path));
        }

        path = safeGet(uri::toString);
        if (path != null && !path.isEmpty()) {
            return Optional.of(cleanUriPath(path));
        }

        path = safeGet(uri::getSchemeSpecificPart);
        if (path != null && !path.isEmpty()) {
            return Optional.of(cleanUriPath(path));
        }

        return Optional.empty();
    }

    private String safeGet(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanUriPath(String path) {
        if (path.contains(":") && !path.startsWith("/")) {
            int colonIndex = path.indexOf(":");
            if (colonIndex < path.length() - 1) {
                return path.substring(colonIndex + 1);
            }
        }
        return path;
    }

    private String extractFileNameFromPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash != -1 ? path.substring(lastSlash + 1) : path;
    }

    private String removeFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot != -1 ? fileName.substring(0, lastDot) : fileName;
    }

    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Could not get stack trace: " + throwable.getClass().getName();
        }
    }
}