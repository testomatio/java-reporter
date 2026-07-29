package io.testomat.agent;

import io.testomat.advice.AttachmentAdvice;
import io.testomat.allure.AllureClient;
import io.testomat.resolver.AttachmentFileResolver;
import io.testomat.testomat.TestomatClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AttachmentAdviceTest {

    private AllureClient allure;
    private TestomatClient testomatio;
    private AttachmentFileResolver resolver;
    private AttachmentAdvice.AttachmentHandler handler;

    @BeforeEach
    void setUp() {
        allure = mock(AllureClient.class);
        testomatio = mock(TestomatClient.class);
        resolver = mock(AttachmentFileResolver.class);
        handler = new AttachmentAdvice.AttachmentHandler(allure, testomatio, resolver);
    }

    @Test
    void sendsTestAttachment() throws IOException {
        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("test"));
        when(resolver.find("uuid")).thenReturn("file.txt");

        handler.prepare("uuid", "file", "text/plain");
        handler.write("uuid", "data".getBytes());

        verify(testomatio).artifact("file.txt");
    }

    @Test
    void sendsStepAttachment() throws IOException {
        when(allure.getCurrentTest()).thenReturn(Optional.of("test"));
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.of("step"));
        when(resolver.find("uuid")).thenReturn("file.txt");

        handler.prepare("uuid", "file", "text/plain");
        handler.write("uuid", new ByteArrayInputStream("data".getBytes()));

        verify(testomatio).stepArtifact("file.txt");
    }

    @Test
    void ignoresFixtureAndUnknownAttachments() throws IOException {
        when(allure.getCurrentTest()).thenReturn(Optional.empty());
        when(allure.getCurrentTestOrStep()).thenReturn(Optional.empty());
        when(resolver.find("fixture")).thenReturn("file.txt");

        handler.prepare("fixture", "file", "text/plain");
        handler.write("fixture", "data".getBytes());
        handler.write("unknown", "data".getBytes());

        verifyNoInteractions(testomatio);
    }
}
