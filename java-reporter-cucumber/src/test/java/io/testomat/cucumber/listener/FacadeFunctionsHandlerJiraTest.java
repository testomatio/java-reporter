package io.testomat.cucumber.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.jira.JiraStorage;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.cucumber.extractor.TestDataExtractor;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FacadeFunctionsHandler Jira Tests")
class FacadeFunctionsHandlerJiraTest {

    private static final UUID TEST_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private FacadeFunctionsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FacadeFunctionsHandler(
                mock(AwsService.class),
                mock(JsonlService.class),
                mock(TestDataExtractor.class),
                mock(PropertyProvider.class));
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @AfterEach
    void tearDown() {
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @Test
    @DisplayName("Should move Jira links to linked storage by test case id")
    void shouldMoveJiraLinksToLinkedStorage() {
        TestCaseFinished event = createFinishedEvent();
        Testomatio.linkJira("https://jira/browse/PROJ-1");

        handler.handleFacadeFunctions(event);

        assertEquals(List.of("https://jira/browse/PROJ-1"),
                JiraStorage.getLinkedJiraStorage().get(TEST_ID.toString()));
    }

    @Test
    @DisplayName("Should do nothing when no Jira links are stored")
    void shouldDoNothingWithoutJiraLinks() {
        TestCaseFinished event = createFinishedEvent();

        handler.handleFacadeFunctions(event);

        assertNull(JiraStorage.getLinkedJiraStorage().get(TEST_ID.toString()));
    }

    private TestCaseFinished createFinishedEvent() {
        TestCase testCase = mock(TestCase.class);
        when(testCase.getId()).thenReturn(TEST_ID);
        TestCaseFinished event = mock(TestCaseFinished.class);
        when(event.getTestCase()).thenReturn(testCase);
        return event;
    }
}
