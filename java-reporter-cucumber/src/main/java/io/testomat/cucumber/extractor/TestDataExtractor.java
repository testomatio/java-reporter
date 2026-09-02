package io.testomat.cucumber.extractor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStep;
import io.testomat.core.model.ExceptionDetails;
import io.testomat.cucumber.exception.StatusNormalizerException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts test metadata and parameters from Cucumber test case finished events.
 * Handles test identification, status normalization, and parameter extraction.
 */
public class TestDataExtractor {
    private static final Logger log = LoggerFactory.getLogger(TestDataExtractor.class);
    private static final String QUOTED_PATTERN = "[\"']([^\"']+)[\"']|\\b(\\d+(?:\\.\\d+)?)\\b";
    private static final String TEST_ID_REGEX = "@T[a-z0-9]{8}";
    private static final String UNKNOWN_TEST = "Unknown test";
    private static final String TITLE_PREFIX = "@title:";

    /**
     * Creates parameter map from test step text values.
     * Extracts quoted strings and numeric values from Cucumber step definitions.
     *
     * @param event the Cucumber test case finished event
     * @return map containing extracted parameter values
     */
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

    /**
     * Generates a unique run ID for a Cucumber test case.
     * Combines the feature URI, scenario name and parameter values extracted from step text.
     * Simple values are appended directly, complex values are hashed.
     *
     * @param event the Cucumber test case finished event
     * @return the generated run ID
     */
    public String generateRid(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        StringBuilder ridBuilder = new StringBuilder();
        ridBuilder.append(testCase.getUri())
                .append(".")
                .append(testCase.getName());

        Map<String, Object> params = new LinkedHashMap<>();
        List<TestStep> testSteps = testCase.getTestSteps();
        if (testSteps != null) {
            for (TestStep testStep : testSteps) {
                if (testStep instanceof PickleStepTestStep) {
                    String stepText = ((PickleStepTestStep) testStep).getStepText();
                    params.putAll(extractValuesFromStepText(stepText));
                }
            }
        }

        if (params.isEmpty()) {
            return ridBuilder.toString();
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object param = entry.getValue();
            String paramString = param != null ? param.toString() : "null";
            String paramName = entry.getKey();

            if (paramString.length() <= 20 && paramString.matches("[a-zA-Z0-9._-]+")) {
                ridBuilder.append("-").append(paramName).append("_").append(paramString);
            } else {
                int hash = Math.abs(paramString.hashCode());
                ridBuilder.append("-").append(paramName).append("_h").append(hash);
            }
        }

        return ridBuilder.toString();
    }

    /**
     * Extracts exception details from test execution result.
     *
     * @param event the Cucumber test case finished event
     * @return exception details if error exists, empty details otherwise
     */
    public ExceptionDetails extractExceptionDetails(TestCaseFinished event) {
        if (event.getResult().getError() != null) {
            return createExceptionDetails(event.getResult().getError());
        }
        return ExceptionDetails.empty();
    }

    /**
     * Extracts test ID from Cucumber tags.
     * Looks for tags matching the pattern @T[a-z0-9]{8}.
     *
     * @param event the Cucumber test case finished event
     * @return test ID if found, null otherwise
     */
    public String extractTestId(TestCaseEvent event) {
        if (event == null) {
            return null;
        }
        TestCase testCase = event.getTestCase();
        if (testCase == null || testCase.getTags() == null) {
            return null;
        }

        return testCase.getTags().stream()
                .filter(tag -> tag != null && tag.matches(TEST_ID_REGEX))
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts test title from Cucumber tags or scenario name.
     * Looks for @title: tags first, falls back to scenario name.
     *
     * @param event the Cucumber test case finished event
     * @return test title or scenario name
     */
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

    /**
     * Extracts feature file name from test case URI.
     * Handles both Unix and Windows path formats.
     *
     * @param event the Cucumber test case finished event
     * @return feature file name, null if extraction fails
     */
    public String extractFileName(TestCaseFinished event) {
        try {
            return event.getTestCase().getUri().toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Normalizes Cucumber test status to standard format.
     *
     * @param event the Cucumber test case finished event
     * @return normalized status (PASSED, FAILED, or SKIPPED)
     * @throws StatusNormalizerException if result is null
     */
    public String getNormalizedStatus(TestCaseFinished event) {
        Result result = event.getResult();
        if (result == null) {
            throw new StatusNormalizerException("Result is null from event: " + event);
        }

        return normalizeStatus(event.getResult().getStatus());
    }

    private Map<String, Object> extractValuesFromStepText(String stepText) {
        Map<String, Object> values = new LinkedHashMap<>();

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
