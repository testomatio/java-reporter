package io.testomat.karate.hooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.jira.JiraStorage;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.karate.extractor.TestDataExtractor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FacadeFunctionsHandlerTest {

    @Mock
    AwsService awsService;
    @Mock
    JsonlService jsonlService;
    @Mock
    TestDataExtractor dataExtractor;
    @Mock
    ScenarioRuntime sr;
    @Mock
    PropertyProvider provider;

    FacadeFunctionsHandler handler;

    @BeforeEach
    void init() {
        handler = new FacadeFunctionsHandler(provider, awsService, jsonlService, dataExtractor);
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @Test
    void shouldUploadArtifacts() {
        when(dataExtractor.getRid(sr)).thenReturn("rId");
        when(dataExtractor.extractTitle(sr)).thenReturn("Karate Test");
        when(dataExtractor.extractTestId(sr)).thenReturn("Tabcd1234");

        handler.handleFacadeFunctions(sr);

        verify(awsService)
            .uploadAllArtifactsForTest("Karate Test", "rId", "Tabcd1234");
    }

    @Test
    void shouldMoveJiraLinksToLinkedStorage() {
        when(dataExtractor.getRid(sr)).thenReturn("karate-rid");
        when(dataExtractor.extractTitle(sr)).thenReturn("Karate Test");
        when(dataExtractor.extractTestId(sr)).thenReturn("Tabcd1234");
        Testomatio.linkJira("https://jira/browse/PROJ-1");

        handler.handleFacadeFunctions(sr);

        assertEquals(List.of("https://jira/browse/PROJ-1"),
                JiraStorage.getLinkedJiraStorage().get("karate-rid"));
    }

    @Test
    void shouldDoNothingWithoutJiraLinks() {
        when(dataExtractor.getRid(sr)).thenReturn("karate-rid");
        when(dataExtractor.extractTitle(sr)).thenReturn("Karate Test");
        when(dataExtractor.extractTestId(sr)).thenReturn("Tabcd1234");

        handler.handleFacadeFunctions(sr);

        assertEquals(null, JiraStorage.getLinkedJiraStorage().get("karate-rid"));
    }
}

