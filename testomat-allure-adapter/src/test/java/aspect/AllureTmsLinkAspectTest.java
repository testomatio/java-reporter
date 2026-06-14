package aspect;

import io.testomat.aspect.AllureTmsLinkAspect;
import io.testomat.resolver.AllureTmsResolver;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllureTmsLinkAspectTest {

    private AllureTmsResolver resolver;
    private AllureTmsLinkAspect aspect;

    @BeforeEach
    void setup() {
        resolver = mock(AllureTmsResolver.class);
        aspect = new AllureTmsLinkAspect(resolver);
    }

    static class TestClass {
        void testMethod() {
        }
    }

    @Test
    void shouldReturnExistingTestId() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn("TMS-123");

        Object result = aspect.intercept(pjp);

        assertThat(result).isEqualTo("TMS-123");

        verify(pjp).proceed();
        verifyNoInteractions(resolver);
    }

    @Test
    void shouldResolveTestIdWhenExtractorReturnsNull() throws Throwable {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn(null);
        when(pjp.getArgs()).thenReturn(new Object[]{method});
        when(resolver.resolve(method)).thenReturn("TMS-456");

        Object result = aspect.intercept(pjp);

        assertThat(result).isEqualTo("TMS-456");

        verify(pjp).proceed();
        verify(resolver).resolve(method);
    }

    @Test
    void shouldResolveTestIdWhenExtractorReturnsBlank() throws Throwable {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn("   ");
        when(pjp.getArgs()).thenReturn(new Object[]{method});
        when(resolver.resolve(method)).thenReturn("TMS-789");

        Object result = aspect.intercept(pjp);

        assertThat(result).isEqualTo("TMS-789");

        verify(pjp).proceed();
        verify(resolver).resolve(method);
    }

    @Test
    void shouldReturnNullWhenNothingResolved() throws Throwable {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn(null);
        when(pjp.getArgs()).thenReturn(new Object[]{method});
        when(resolver.resolve(method)).thenReturn(null);

        Object result = aspect.intercept(pjp);

        assertThat(result).isNull();

        verify(pjp).proceed();
        verify(resolver).resolve(method);
    }

    @Test
    void shouldReturnBlankWhenResolverReturnsNull() throws Throwable {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn("");
        when(pjp.getArgs()).thenReturn(new Object[]{method});
        when(resolver.resolve(method)).thenReturn(null);

        Object result = aspect.intercept(pjp);

        assertThat(result).isEqualTo("");

        verify(pjp).proceed();
        verify(resolver).resolve(method);
    }
}