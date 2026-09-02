package resolver;

import io.testomat.resolver.TestNgResolver;
import org.testng.annotations.Test;
import org.junit.jupiter.api.Assertions;
import java.lang.reflect.Method;

class TestNgResolverTest {

    TestNgResolver resolver = new TestNgResolver();

    static class TestClass {

        @Test(description = "TestNG title")
        void testWithDescription() {}

        @Test(description = "")
        void testWithEmptyDescription() {}

        void testWithoutAnnotation() {}

    }

    @org.junit.jupiter.api.Test
    void shouldResolveDescription() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithDescription");
        String result = resolver.resolve(method);
        Assertions.assertEquals("TestNG title", result);
    }

    @org.junit.jupiter.api.Test
    void shouldReturnNullIfDescriptionEmpty() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithEmptyDescription");
        String result = resolver.resolve(method);
        Assertions.assertNull(result);
    }

    @org.junit.jupiter.api.Test
    void shouldReturnNullIfNoAnnotation() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithoutAnnotation");
        String result = resolver.resolve(method);
        Assertions.assertNull(result);
    }

}
