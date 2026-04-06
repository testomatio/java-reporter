package aspect;

import io.testomat.aspect.TitleAspect;
import io.testomat.resolver.TestTitleResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TitleAspectTest {

    TitleAspect aspect = new TitleAspect();

    static class TestClass {
        void testMethod() {}
    }

    @Test
    void shouldResolveTitle() throws Exception {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        when(joinPoint.getArgs()).thenReturn(new Object[]{method});

        try (MockedStatic<TestTitleResolver> resolver = mockStatic(TestTitleResolver.class)) {
            resolver.when(() -> TestTitleResolver.resolve(method)).thenReturn("TITLE");
            Object result = aspect.intercept(joinPoint);

            assertThat(result).isEqualTo("TITLE");
            resolver.verify(() -> TestTitleResolver.resolve(method));

        }
    }

}
