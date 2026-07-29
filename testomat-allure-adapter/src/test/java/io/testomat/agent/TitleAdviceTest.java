package io.testomat.agent;

import io.testomat.advice.TitleAdvice;
import io.testomat.resolver.TestTitleResolver;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class TitleAdviceTest {

    @Test
    void resolvesTitle() throws NoSuchMethodException {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        try (MockedStatic<TestTitleResolver> resolver = mockStatic(TestTitleResolver.class)) {
            resolver.when(() -> TestTitleResolver.resolve(method)).thenReturn("TITLE");

            String title = TitleAdvice.resolve(method);

            assertThat(title).isEqualTo("TITLE");
            resolver.verify(() -> TestTitleResolver.resolve(method));
        }
    }

    private static class TestClass {
        void testMethod() {
        }
    }
}
