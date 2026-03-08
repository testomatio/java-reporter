package io.testomat.karate.extractor;

import static io.testomat.core.constants.CommonConstants.FAILED;
import static io.testomat.core.constants.CommonConstants.PASSED;
import static io.testomat.core.constants.CommonConstants.SKIPPED;

import com.intuit.karate.core.ScenarioResult;
import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.Testomatio;
import io.testomat.core.model.ExceptionDetails;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts test metadata and execution details from finished Karate test cases.
 * <p>
 * Handles test identification, status normalization, and parameter extraction.
 */
public class TestDataExtractor {

    private static final Logger log = LoggerFactory.getLogger(TestDataExtractor.class);
    private static final String TEST_ID_REGEX = "T[a-z0-9]{8}";
    private static final String TEST_ID_PREFIX = "testid";
    private static final String TITLE_PREFIX = "title:";
    private static final String ATTACHMENTS_PREFIX = "attachments:";

    /**
     * Extracts exception details from a Karate test case execution result.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     * @return exception details if an error exists; otherwise, empty exception details
     */
    public ExceptionDetails extractExceptionDetails(ScenarioRuntime sr) {
        if (sr.result.isFailed()) {
            return createExceptionDetails(sr.result.getError());
        }
        return ExceptionDetails.empty();
    }

    /**
     * Extracts a test identifier from scenario tags.
     * <p>
     * Searches for the first tag that starts with the configured {@code TEST_ID_PREFIX}
     * (case-insensitive), removes the prefix, and validates the remaining value.
     *
     * @param sr the {@link ScenarioRuntime} of the executed Karate scenario
     * @return the extracted test identifier, or {@code null} if none is found
     */
    public String extractTestId(ScenarioRuntime sr) {
        return findFirstValidTestId(sr.tags.getTags().stream()
            .filter(Objects::nonNull)
            .filter(tag -> tag.regionMatches(true, 0,
                TEST_ID_PREFIX, 0, TEST_ID_PREFIX.length()))
            .map(tag -> tag.substring(TEST_ID_PREFIX.length() + 1)));

    }

    /**
     * Extracts the test title from Karate tags or the scenario name.
     * <p>
     * Looks for {@code @title:} tags first and falls back to the scenario name
     * if no title tag is found.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     * @return the extracted test title or the scenario name if no title tag is present
     */
    public String extractTitle(ScenarioRuntime sr) {
        return sr.tags.getTags().stream()
            .filter(Objects::nonNull)
            .filter(tag -> tag.regionMatches(true, 0,
                TITLE_PREFIX, 0, TITLE_PREFIX.length()))
            .map(tag -> tag.substring(TITLE_PREFIX.length()))
            .map(t -> t.replace('_', ' '))
            .findFirst()
            .orElseGet(() -> getTestName(sr));
    }

    /**
     * Extracts the feature file name from the test case resource path.
     * Handles both Unix and Windows path formats.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     * @return the feature file name, or {@code null} if extraction fails
     */
    public String extractFileName(ScenarioRuntime sr) {
        try {
            return sr.scenario.getFeature().getResource().getRelativePath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts attachment references from Karate test case tags
     * and registers them as test artifacts.
     * <p>
     * Attachment tags are expected to start with the configured attachments prefix
     * (for example, {@code @attachments:}). The tag value may contain one or more
     * comma-separated attachment identifiers.
     * <p>
     * Empty or blank attachment values are ignored. If at least one attachment is
     * extracted, the attachments are registered via {@link Testomatio#artifact(String...)}.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     */
    public void extractAttachments(ScenarioRuntime sr) {
        String[] attachments = sr.tags.getTags().stream()
            .filter(Objects::nonNull)
            .filter(tag -> tag.regionMatches(true, 0,
                ATTACHMENTS_PREFIX, 0, ATTACHMENTS_PREFIX.length()))
            .map(tag -> tag.substring(ATTACHMENTS_PREFIX.length()).trim())
            .filter(s -> !s.isEmpty())
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

        if (attachments.length > 0) {
            Testomatio.artifact(attachments);
        }
    }

    /**
     * Normalizes a Karate test case execution status to a standard format.
     *
     * @param sr the {@link ScenarioRuntime} representing the finished Karate test case
     * @return the normalized status: {@code PASSED}, {@code FAILED}, or {@code SKIPPED}
     */
    public String getNormalizedStatus(ScenarioRuntime sr) {
        ScenarioResult result = sr.result;
        return normalizeStatus(result);
    }

    /**
     * Generates a stable runtime identifier (rId) for a Karate test scenario execution.
     * <p>
     * The identifier is deterministically derived from the scenario location
     * (feature file path and scenario line number). This ensures that the same
     * scenario always produces the same rId across multiple test runs.
     * <p>
     * The rId is generated using a name-based UUID calculated from the string:
     * <pre>
     *     &lt;feature-relative-path&gt;:&lt;scenario-line-number&gt;
     * </pre>
     *
     * @param sr the {@link ScenarioRuntime} representing the executed Karate test scenario
     * @return a deterministic UUID string uniquely identifying the scenario execution
     */
    public String getRid(ScenarioRuntime sr) {
        String raw = String.format("%s:%s",
                sr.scenario.getFeature().getResource().getRelativePath(),
                sr.scenario.getLine()
        );

        return UUID.nameUUIDFromBytes(raw.getBytes()).toString();
    }

    private String findFirstValidTestId(Stream<String> ids) {
        return ids
            .filter(id -> id.matches(TEST_ID_REGEX))
            .findFirst()
            .orElse(null);
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
