package resolver;

import io.testomat.resolver.TestMetadataResolver;
import io.testomat.resolver.TestTitleResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestTitleResolverTest {

    static class TestClass {
        void testMethod() {}
    }

    @AfterEach
    void cleanup() {
        TestTitleResolver.setResolvers(List.of());
    }

    @Test
    void shouldReturnTitleFromResolver() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        TestMetadataResolver resolver = mock(TestMetadataResolver.class);
        when(resolver.resolve(method)).thenReturn("TITLE");
        TestTitleResolver.setResolvers(List.of(resolver));
        String result = TestTitleResolver.resolve(method);

        assertThat(result).isEqualTo("TITLE");
        verify(resolver).resolve(method);
    }

    @Test
    void shouldFallbackToMethodName() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        TestMetadataResolver resolver = mock(TestMetadataResolver.class);

        when(resolver.resolve(method)).thenReturn(null);
        TestTitleResolver.setResolvers(List.of(resolver));
        String result = TestTitleResolver.resolve(method);

        assertThat(result).isEqualTo("testMethod");
    }

    @Test
    void shouldUseFirstNonNullResolver() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testMethod");

        TestMetadataResolver first = mock(TestMetadataResolver.class);
        TestMetadataResolver second = mock(TestMetadataResolver.class);

        when(first.resolve(method)).thenReturn(null);
        when(second.resolve(method)).thenReturn("SECOND");

        TestTitleResolver.setResolvers(List.of(first, second));

        String result = TestTitleResolver.resolve(method);

        assertThat(result).isEqualTo("SECOND");
        verify(first).resolve(method);
        verify(second).resolve(method);
    }

    @Test
    void shouldStopAfterFirstMatch() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testMethod");
        TestMetadataResolver first = mock(TestMetadataResolver.class);
        TestMetadataResolver second = mock(TestMetadataResolver.class);
        when(first.resolve(method)).thenReturn("FIRST");
        TestTitleResolver.setResolvers(List.of(first, second));
        String result = TestTitleResolver.resolve(method);

        assertThat(result).isEqualTo("FIRST");
        verify(first).resolve(method);
        verify(second, never()).resolve(method);
    }

}