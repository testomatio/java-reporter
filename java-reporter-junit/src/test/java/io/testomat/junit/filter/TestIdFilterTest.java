package io.testomat.junit.filter;

import io.testomat.core.annotation.TestId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import java.lang.reflect.Method;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestIdFilterTest {

    private static final String IDS = "ids";

    @AfterEach
    void cleanup() {
        System.clearProperty(IDS);
    }

    @Test
    void shouldIncludeAllWhenIdsNotSpecified() {
        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = mock(TestDescriptor.class);
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldIncludeMatchingTestId() throws Exception {
        System.setProperty(IDS, "TEST-123");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = descriptor(TestClass.class.getDeclaredMethod("test1"));
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldExcludeNonMatchingTestId() throws Exception {
        System.setProperty(IDS, "TEST-999");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = descriptor(TestClass.class.getDeclaredMethod("test1"));
        FilterResult result = filter.apply(descriptor);

        assertThat(result.excluded()).isTrue();
    }

    @Test
    void shouldExcludeTestWithoutTestIdAnnotation() throws Exception {
        System.setProperty(IDS, "TEST-123");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = descriptor(TestClass.class.getDeclaredMethod("noId"));
        FilterResult result = filter.apply(descriptor);

        assertThat(result.excluded()).isTrue();
    }

    @Test
    void shouldSupportMultipleIds() throws Exception {
        System.setProperty(IDS, "TEST-123,TEST-456");

        TestIdFilter filter = new TestIdFilter();
        FilterResult r1 = filter.apply(descriptor(
            TestClass.class.getDeclaredMethod("test1")));
        FilterResult r2 = filter.apply(descriptor(
            TestClass.class.getDeclaredMethod("test2")));

        assertThat(r1.included()).isTrue();
        assertThat(r2.included()).isTrue();
    }

    @Test
    void shouldTrimWhitespace() throws Exception {
        System.setProperty(IDS, " TEST-123 ");

        TestIdFilter filter = new TestIdFilter();
        FilterResult result = filter.apply(descriptor(
            TestClass.class.getDeclaredMethod("test1")));

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldIncludeWhenNoSource() {
        System.setProperty(IDS, "TEST-123");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = mock(TestDescriptor.class);
        when(descriptor.getSource())
            .thenReturn(Optional.empty());
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldIncludeWhenSourceNotMethodSource() {
        System.setProperty(IDS, "TEST-123");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = mock(TestDescriptor.class);
        TestSource source = mock(TestSource.class);
        when(descriptor.getSource())
            .thenReturn(Optional.of(source));
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldIncludeWhenMethodIsNull() {
        System.setProperty(IDS, "TEST-123");

        TestIdFilter filter = new TestIdFilter();
        MethodSource source = mock(MethodSource.class);
        when(source.getJavaMethod())
            .thenReturn(null);
        TestDescriptor descriptor = mock(TestDescriptor.class);
        when(descriptor.getSource())
            .thenReturn(Optional.of(source));
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldSupportSpecialCharacters() throws Exception {
        System.setProperty(IDS, "TEST-123_v2.0");

        TestIdFilter filter = new TestIdFilter();
        FilterResult result = filter.apply(descriptor(
            TestClass.class.getDeclaredMethod("special")));

        assertThat(result.included()).isTrue();
    }

    @Test
    void shouldHandleEmptyProperty() {
        System.setProperty(IDS,"");

        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = mock(TestDescriptor.class);
        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    private TestDescriptor descriptor(Method method) {
        MethodSource source = mock(MethodSource.class);
        when(source.getJavaMethod())
            .thenReturn(method);
        TestDescriptor descriptor = mock(TestDescriptor.class);
        when(descriptor.getSource())
            .thenReturn(Optional.of(source));

        return descriptor;
    }

    static class TestClass {
        @TestId("TEST-123")
        void test1(){}

        @TestId("TEST-456")
        void test2(){}

        void noId(){}

        @TestId("TEST-123_v2.0")
        void special(){}
    }
}
