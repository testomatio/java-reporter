package io.testomat.karate.hooks;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intuit.karate.core.ScenarioRuntime;
import io.testomat.core.facade.methods.artifact.client.AwsService;
import io.testomat.karate.extractor.TestDataExtractor;
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
    TestDataExtractor dataExtractor;
    @Mock
    ScenarioRuntime sr;

    FacadeFunctionsHandler handler;

    @BeforeEach
    void init() {
        handler = new FacadeFunctionsHandler(awsService, dataExtractor);
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
}

