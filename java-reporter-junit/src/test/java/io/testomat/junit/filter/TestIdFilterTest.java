package io.testomat.junit.filter;

import io.testomat.core.annotation.TestId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for TestIdFilter focusing on correct filtering behavior
 * with various system property configurations and test scenarios.
 */
class TestIdFilterTest {

    @AfterEach
    void cleanup() {
        // Clean up system property after each test
        System.clearProperty("ids");
    }

    @Test
    void apply_shouldIncludeAllWhenNoIdsPropertySet() {
        TestIdFilter filter = new TestIdFilter();
        TestDescriptor descriptor = createMockDescriptor();

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldIncludeTestWithMatchingId() throws Exception {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldExcludeTestWithNonMatchingId() throws Exception {
        System.setProperty("ids", "TEST-999");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.excluded()).isTrue();
    }

    @Test
    void apply_shouldIncludeTestWithoutAnnotationWhenFiltering() throws Exception {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithoutId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldHandleMultipleIdsCommaSeparated() throws Exception {
        System.setProperty("ids", "TEST-123,TEST-456,TEST-789");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod1 = TestClass.class.getMethod("testWithId");
        Method testMethod2 = TestClass.class.getMethod("anotherTestWithId");

        TestMethodTestDescriptor descriptor1 = createMethodDescriptor(testMethod1);
        TestMethodTestDescriptor descriptor2 = createMethodDescriptor(testMethod2);

        FilterResult result1 = filter.apply(descriptor1);
        FilterResult result2 = filter.apply(descriptor2);

        assertThat(result1.included()).isTrue();
        assertThat(result2.included()).isTrue();
    }

    @Test
    void apply_shouldTrimWhitespaceInIds() throws Exception {
        System.setProperty("ids", " TEST-123 , TEST-456 , TEST-789 ");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldHandleEmptyIdsProperty() {
        System.setProperty("ids", "");
        TestIdFilter filter = new TestIdFilter();

        TestDescriptor descriptor = createMockDescriptor();

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldIncludeNonTestMethodDescriptor() {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        TestDescriptor nonMethodDescriptor = createMockDescriptor();

        FilterResult result = filter.apply(nonMethodDescriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldHandleSingleIdFilter() throws Exception {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void apply_shouldBeConsistentAcrossMultipleCalls() throws Exception {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result1 = filter.apply(descriptor);
        FilterResult result2 = filter.apply(descriptor);

        assertThat(result1.included()).isEqualTo(result2.included());
    }

    @Test
    void apply_shouldFilterMultipleDifferentTests() throws Exception {
        System.setProperty("ids", "TEST-123");
        TestIdFilter filter = new TestIdFilter();

        Method includedMethod = TestClass.class.getMethod("testWithId");
        Method excludedMethod = TestClass.class.getMethod("anotherTestWithId");
        Method noIdMethod = TestClass.class.getMethod("testWithoutId");

        TestMethodTestDescriptor includedDescriptor = createMethodDescriptor(includedMethod);
        TestMethodTestDescriptor excludedDescriptor = createMethodDescriptor(excludedMethod);
        TestMethodTestDescriptor noIdDescriptor = createMethodDescriptor(noIdMethod);

        FilterResult includedResult = filter.apply(includedDescriptor);
        FilterResult excludedResult = filter.apply(excludedDescriptor);
        FilterResult noIdResult = filter.apply(noIdDescriptor);

        assertThat(includedResult.included()).isTrue();
        assertThat(excludedResult.excluded()).isTrue();
        assertThat(noIdResult.included()).isTrue();
    }

    @Test
    void apply_shouldHandleSpecialCharactersInTestId() throws Exception {
        System.setProperty("ids", "TEST-123_v2.0");
        TestIdFilter filter = new TestIdFilter();

        Method testMethod = TestClass.class.getMethod("testWithSpecialId");
        TestMethodTestDescriptor descriptor = createMethodDescriptor(testMethod);

        FilterResult result = filter.apply(descriptor);

        assertThat(result.included()).isTrue();
    }

    @Test
    void constructor_shouldInitializeCorrectlyWithIds() {
        System.setProperty("ids", "TEST-1,TEST-2");

        TestIdFilter filter = new TestIdFilter();

        assertThat(filter).isNotNull();
    }

    @Test
    void constructor_shouldInitializeCorrectlyWithoutIds() {
        TestIdFilter filter = new TestIdFilter();

        assertThat(filter).isNotNull();
    }

    private TestDescriptor createMockDescriptor() {
        return mock(TestDescriptor.class);
    }

    private TestMethodTestDescriptor createMethodDescriptor(Method method) {
        TestMethodTestDescriptor descriptor = mock(TestMethodTestDescriptor.class);
        when(descriptor.getTestMethod()).thenReturn(method);
        return descriptor;
    }

    // Test class with various test methods
    static class TestClass {
        @TestId("TEST-123")
        public void testWithId() {
        }

        @TestId("TEST-456")
        public void anotherTestWithId() {
        }

        public void testWithoutId() {
        }

        @TestId("TEST-123_v2.0")
        public void testWithSpecialId() {
        }
    }
}
