package io.testomat.cucumber.extractor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStep;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.cucumber.exception.StatusNormalizerException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDataExtractor {
    private static final Logger log = LoggerFactory.getLogger(TestDataExtractor.class);
    private static final String QUOTED_PATTERN = "[\"']([^\"']+)[\"']|\\b(\\d+(?:\\.\\d+)?)\\b";
    private static final String TEST_ID_REGEX = "@T[a-z0-9]{8}";
    private static final String UNKNOWN_TEST = "Unknown test";
    private static final String TITLE_PREFIX = "@title:";

    public Map<Object, Object> createExample(TestCaseFinished event) {

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

    public ExceptionDetails extractExceptionDetails(TestCaseFinished event) {
        if (event.getResult().getError() != null) {
            return createExceptionDetails(event.getResult().getError());
        }
        return ExceptionDetails.empty();
    }

    public String extractTestId(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        if (testCase == null || testCase.getTags() == null) {
            return null;
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.matches(TEST_ID_REGEX))
                .findFirst()
                .orElse(null);
    }

    public String extractTitle(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        if (testCase == null || testCase.getTags() == null) {
            return getTestName(testCase);
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.toLowerCase().startsWith(TITLE_PREFIX))
                .map(tag -> tag.substring(TITLE_PREFIX.length()).replace("_", " "))
                .findFirst()
                .orElse(getTestName(testCase));
    }

    public String extractFileName(TestCaseFinished event) {
        try {
            String path = event.getTestCase().getUri().getPath();

            if (path.contains(":") && !path.startsWith("/")) {
                int colonIndex = path.indexOf(":");
                if (colonIndex < path.length() - 1) {
                    path = path.substring(colonIndex + 1);
                }
            }

            int lastSlash = path.lastIndexOf('/');
            return lastSlash != -1 ? path.substring(lastSlash + 1) : path;

        } catch (Exception e) {
            return null;
        }
    }

    public String getNormalizedStatus(TestCaseFinished event) {
        Result result = event.getResult();
        if (result == null) {
            throw new StatusNormalizerException("Result is null from event: " + event);
        }

        return normalizeStatus(event.getResult().getStatus());
    }

    private Map<String, Object> extractValuesFromStepText(String stepText) {
        Map<String, Object> values = new HashMap<>();

        Pattern quotedPattern = Pattern.compile(QUOTED_PATTERN);
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

    private ExceptionDetails createExceptionDetails(Throwable throwable) {
        String message = throwable.getMessage();
        String stack = getStackTrace(throwable);
        log.debug("Including error details for failed test");
        return new ExceptionDetails(message, stack);
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private String getTestName(TestCase testCase) {
        if (testCase == null) {
            return UNKNOWN_TEST;
        }

        try {
            return testCase.getName() != null ? testCase.getName() : UNKNOWN_TEST;
        } catch (Exception e) {
            return UNKNOWN_TEST;
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
