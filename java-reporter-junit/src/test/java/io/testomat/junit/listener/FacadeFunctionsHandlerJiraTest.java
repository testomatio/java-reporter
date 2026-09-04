package io.testomat.junit.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.jira.JiraStorage;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

@DisplayName("FacadeFunctionsHandler Jira Tests")
class FacadeFunctionsHandlerJiraTest {

    private FacadeFunctionsHandler handler;
    private ExtensionContext context;

    @BeforeEach
    void setUp() throws Exception {
        Method testMethod = FacadeFunctionsHandlerJiraTest.class
                .getDeclaredMethod("shouldMoveJiraLinksToLinkedStorage");
        context = mock(ExtensionContext.class);
        when(context.getTestMethod()).thenReturn(Optional.of(testMethod));
        handler = new FacadeFunctionsHandler(
                false,
                mock(PropertyProvider.class),
                mock(AwsService.class),
                mock(JsonlService.class));
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @AfterEach
    void tearDown() {
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @Test
    @DisplayName("Should move Jira links to linked storage by test unique id")
    void shouldMoveJiraLinksToLinkedStorage() {
        when(context.getUniqueId()).thenReturn("rid-1");
        Testomatio.linkJira("https://jira/browse/PROJ-1");

        handler.handleFacadeFunctions(context);

        assertEquals(List.of("https://jira/browse/PROJ-1"),
                JiraStorage.getLinkedJiraStorage().get("rid-1"));
    }

    @Test
    @DisplayName("Should do nothing when no Jira links are stored")
    void shouldDoNothingWithoutJiraLinks() {
        when(context.getUniqueId()).thenReturn("rid-1");

        handler.handleFacadeFunctions(context);

        assertNull(JiraStorage.getLinkedJiraStorage().get("rid-1"));
    }
}
