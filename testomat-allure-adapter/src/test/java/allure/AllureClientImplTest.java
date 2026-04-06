package allure;

import io.qameta.allure.AllureLifecycle;
import io.testomat.allure.AllureClientImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllureClientImplTest {

    @Test
    void shouldReturnCurrentTest() {
        AllureLifecycle lifecycle = mock(AllureLifecycle.class);
        when(lifecycle.getCurrentTestCase()).thenReturn(Optional.of("uuid"));
        AllureClientImpl client = new AllureClientImpl(lifecycle);
        Optional<String> result = client.getCurrentTest();

        assertThat(result).contains("uuid");
        verify(lifecycle).getCurrentTestCase();
    }

    @Test
    void shouldReturnCurrentTestOrStep() {
        AllureLifecycle lifecycle = mock(AllureLifecycle.class);
        when(lifecycle.getCurrentTestCaseOrStep()).thenReturn(Optional.of("step"));
        AllureClientImpl client = new AllureClientImpl(lifecycle);
        Optional<String> result = client.getCurrentTestOrStep();

        assertThat(result).contains("step");
        verify(lifecycle).getCurrentTestCaseOrStep();
    }

}
