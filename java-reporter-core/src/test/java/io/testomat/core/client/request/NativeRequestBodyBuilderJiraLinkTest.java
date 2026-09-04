package io.testomat.core.client.request;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.methods.jira.JiraStorage;
import io.testomat.core.facade.methods.label.LabelStorage;
import io.testomat.core.model.TestResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NativeRequestBodyBuilder Jira Link Tests")
class NativeRequestBodyBuilderJiraLinkTest {

    private static final String RID = "tests.ui.SelenideTest.default-testName-rid";

    private NativeRequestBodyBuilder requestBodyBuilder = new NativeRequestBodyBuilder();

    @BeforeEach
    void setUp() {
        LabelStorage.getLinkedLabelStorage().clear();
        JiraStorage.clearLinkedJiraStorage();
    }

    @AfterEach
    void tearDown() {
        LabelStorage.getLinkedLabelStorage().clear();
        JiraStorage.clearLinkedJiraStorage();
    }

    @Test
    @DisplayName("Should include Jira links even when no labels exist")
    void shouldIncludeJiraLinksWithoutLabels() throws Exception {
        JiraStorage.getLinkedJiraStorage().put(RID, List.of("https://jira/browse/PROJ-1"));
        TestResult result = TestResult.builder().withRid(RID).build();

        String body = requestBodyBuilder.buildSingleTestReportBody(result);

        assertTrue(body.contains("https://jira/browse/PROJ-1"), body);
    }

    @Test
    @DisplayName("Should include both labels and Jira links")
    void shouldIncludeLabelsAndJiraLinks() throws Exception {
        LabelStorage.getLinkedLabelStorage().put(RID, List.of(Map.of("label", "smoke")));
        JiraStorage.getLinkedJiraStorage().put(RID, List.of("https://jira/browse/PROJ-1"));
        TestResult result = TestResult.builder().withRid(RID).build();

        String body = requestBodyBuilder.buildSingleTestReportBody(result);

        assertTrue(body.contains("smoke"), body);
        assertTrue(body.contains("https://jira/browse/PROJ-1"), body);
    }

    @Test
    @DisplayName("Should not duplicate existing Jira links")
    void shouldNotDuplicateJiraLinks() throws Exception {
        JiraStorage.getLinkedJiraStorage().put(RID, List.of("https://jira/browse/PROJ-1"));
        TestResult result = TestResult.builder()
                .withRid(RID)
                .withLinks(List.of(io.testomat.core.model.Link.jira("https://jira/browse/PROJ-1")))
                .build();

        String body = requestBodyBuilder.buildSingleTestReportBody(result);

        assertTrue(body.contains("https://jira/browse/PROJ-1"), body);
        assertFalse(body.contains("PROJ-1\"]"), body);
    }

    @Test
    @DisplayName("Should not add anything when no labels and no Jira links")
    void shouldNotAddAnythingWhenEmpty() throws Exception {
        TestResult result = TestResult.builder().withRid(RID).build();

        String body = requestBodyBuilder.buildSingleTestReportBody(result);

        assertFalse(body.contains("https://jira"), body);
    }
}
