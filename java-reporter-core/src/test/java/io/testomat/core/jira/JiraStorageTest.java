package io.testomat.core.jira;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.testomat.core.facade.Testomatio;
import io.testomat.core.facade.methods.jira.JiraStorage;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JiraStorage Tests")
class JiraStorageTest {

    @BeforeEach
    void setUp() {
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @AfterEach
    void tearDown() {
        JiraStorage.clearTempJiraStorage();
        JiraStorage.clearLinkedJiraStorage();
    }

    @Test
    @DisplayName("Should add single Jira link to temp storage")
    void shouldAddSingleLink() {
        Testomatio.linkJira("https://jira.atlassian.net/browse/PROJ-1");

        assertEquals(
                List.of("https://jira.atlassian.net/browse/PROJ-1"),
                JiraStorage.getTempJiraStorage());
    }

    @Test
    @DisplayName("Should add multiple Jira links preserving order")
    void shouldAddMultipleLinks() {
        Testomatio.linkJira("PROJ-1", "PROJ-2", "PROJ-3");

        assertEquals(List.of("PROJ-1", "PROJ-2", "PROJ-3"),
                JiraStorage.getTempJiraStorage());
    }

    @Test
    @DisplayName("Should ignore null varargs and not throw")
    void shouldHandleNullVarargs() {
        assertDoesNotThrow(() -> Testomatio.linkJira((String[]) null));

        assertTrue(JiraStorage.getTempJiraStorage().isEmpty());
    }

    @Test
    @DisplayName("Should ignore empty varargs and not throw")
    void shouldHandleEmptyVarargs() {
        assertDoesNotThrow(() -> Testomatio.linkJira());

        assertTrue(JiraStorage.getTempJiraStorage().isEmpty());
    }

    @Test
    @DisplayName("Should isolate Jira links per thread")
    void shouldIsolatePerThread() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> otherThreadStorage = new AtomicReference<>();

        Thread otherThread = new Thread(() -> {
            Testomatio.linkJira("THREAD-1");
            otherThreadStorage.set(JiraStorage.getTempJiraStorage());
            latch.countDown();
        });
        otherThread.start();

        Testomatio.linkJira("MAIN-1");
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        otherThread.join();

        assertEquals(List.of("MAIN-1"), JiraStorage.getTempJiraStorage());
        assertEquals(List.of("THREAD-1"), otherThreadStorage.get());
    }

    @Test
    @DisplayName("Should expose empty linked storage initially")
    void shouldExposeLinkedStorage() {
        assertTrue(JiraStorage.getLinkedJiraStorage().isEmpty());
    }
}
