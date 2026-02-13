package io.testomat.karate.extractor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import com.intuit.karate.core.ScenarioResult;
import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.model.ExceptionDetails;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extracts test metadata and parameters from Karate test case finished srs. Handles test identification, status
 * normalization, and parameter extraction.
 */
public class TestDataExtractor {

    private static final Logger log = LoggerFactory.getLogger(TestDataExtractor.class);
    private static final String QUOTED_PATTERN = "[\"']([^\"']+)[\"']|\\b(\\d+(?:\\.\\d+)?)\\b";
    private static final String TEST_ID_REGEX = "T[a-z0-9]{8}";
    private static final String UNKNOWN_TEST = "Unknown test";
    private static final String TITLE_PREFIX = "title:";

    /**
     * Extracts exception details from test execution result.
     *
     * @param sr the Karate test case finished scenario runtime
     * @return exception details if error exists, empty details otherwise
     */
    public ExceptionDetails extractExceptionDetails(ScenarioRuntime sr) {
        if (sr.result.isFailed()) {
            return createExceptionDetails(sr.result.getError());
        }
        return ExceptionDetails.empty();
    }

    /**
     * Extracts test ID from Karate tags. Looks for tags matching the pattern @T[a-z0-9]{8}.
     *
     * @param sr the Karate test case finished sr
     * @return test ID if found, null otherwise
     */
    public String extractTestId(ScenarioRuntime sr) {
        return sr.tags.getTags().stream()
            .filter(Objects::nonNull)
            .filter(tag -> tag.matches(TEST_ID_REGEX))
            .map(tag -> "@" + tag)
            .findFirst()
            .orElse(null);
    }

    /**
     * Extracts test title from Karate tags or scenario name. Looks for @title: tags first, falls back to scenario
     * name.
     *
     * @param sr the Karate test case finished sr
     * @return test title or scenario name
     */
    public String extractTitle(ScenarioRuntime sr) {
        return sr.tags.getTags().stream()
            .filter(tag -> tag != null && tag.toLowerCase().startsWith(TITLE_PREFIX))
            .map(tag -> tag.substring(TITLE_PREFIX.length()).replace("_", " "))
            .findFirst()
            .orElse(getTestName(sr));
    }

    /**
     * Extracts feature file name from test case URI. Handles both Unix and Windows path formats.
     *
     * @param sr the Karate test case finished sr
     * @return feature file name, null if extraction fails
     */
    public String extractFileName(ScenarioRuntime sr) {
        try {
            return sr.scenario.getFeature().getResource().getRelativePath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Normalizes Karate test status to standard format.
     *
     * @param sr the Karate test case finished sr
     * @return normalized status (PASSED, FAILED, or SKIPPED) //     * @throws StatusNormalizerException if result is
     * null
     */
    public String getNormalizedStatus(ScenarioRuntime sr) {
        ScenarioResult result = sr.result;
        return normalizeStatus(result);
    }

    public String getRid(ScenarioRuntime sr) {
        String raw = String.format("%s:%s",
            sr.scenario.getFeature().getResource().getRelativePath(),
            sr.scenario.getLine()
        );

        return UUID.nameUUIDFromBytes(raw.getBytes()).toString();
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

    private String getTestName(ScenarioRuntime sr) {
        return sr.scenario.getName();
    }

    private String normalizeStatus(ScenarioResult sr) {
        if (sr == null || sr.isFailed()) {
            return FAILED;
        }
        return (sr.getStepResults() == null || sr.getStepResults().isEmpty()) ? SKIPPED : PASSED;
    }
}
