package io.testomat.testng.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.core.facade.methods.artifact.client.JsonlService;
import io.testomat.core.facade.methods.jira.JiraStorage;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.testng.extractor.TestNgMetaDataExtractor;
import io.testomat.testng.extractor.TestNgParameterExtractor;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testng.IInvokedMethod;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

@DisplayName("FacadeFunctionsHandler Jira Tests")
class FacadeFunctionsHandlerJiraTest {

    private FacadeFunctionsHandler handler;
    private TestNgParameterExtractor parameterExtractor;
    private IInvokedMethod invocationMethod;

    @BeforeEach
    void setUp() {
        parameterExtractor = mock(TestNgParameterExtractor.class);
        invocationMethod = mock(IInvokedMethod.class);
        ITestNGMethod testNgMethod = mock(ITestNGMethod.class);
        when(invocationMethod.getTestMethod()).thenReturn(testNgMethod);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(mock(ConstructorOrMethod.class));
        handler = new FacadeFunctionsHandler(
                parameterExtractor,
                mock(TestNgMetaDataExtractor.class),
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
    @DisplayName("Should move Jira links to linked storage by rid")
    void shouldMoveJiraLinksToLinkedStorage() {
        ITestResult testResult = mock(ITestResult.class);
        when(parameterExtractor.generateRid(testResult)).thenReturn("rid-1");
        Testomatio.linkJira("https://jira/browse/PROJ-1");

        handler.handleFacadeFunctions(invocationMethod, testResult);

        assertEquals(List.of("https://jira/browse/PROJ-1"),
                JiraStorage.getLinkedJiraStorage().get("rid-1"));
    }

    @Test
    @DisplayName("Should do nothing when no Jira links are stored")
    void shouldDoNothingWithoutJiraLinks() {
        ITestResult testResult = mock(ITestResult.class);
        when(parameterExtractor.generateRid(testResult)).thenReturn("rid-1");

        handler.handleFacadeFunctions(invocationMethod, testResult);

        assertNull(JiraStorage.getLinkedJiraStorage().get("rid-1"));
    }
}
