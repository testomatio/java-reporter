package io.testomat.agent;

import io.testomat.advice.TmsLinkAdvice;
import io.testomat.resolver.AllureTmsResolver;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TmsLinkAdviceTest {

    @Test
    void preservesExistingTestId() throws NoSuchMethodException {
        AllureTmsResolver resolver = mock(AllureTmsResolver.class);
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        String result = TmsLinkAdvice.resolve(method, "TMS-123", resolver);

        assertThat(result).isEqualTo("TMS-123");
        verifyNoInteractions(resolver);
    }

    @Test
    void resolvesMissingTestId() throws NoSuchMethodException {
        AllureTmsResolver resolver = mock(AllureTmsResolver.class);
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        when(resolver.resolve(method)).thenReturn("TMS-456");

        String result = TmsLinkAdvice.resolve(method, "", resolver);

        assertThat(result).isEqualTo("TMS-456");
    }

    private static class TestClass {
        void testMethod() {
        }
    }
}
