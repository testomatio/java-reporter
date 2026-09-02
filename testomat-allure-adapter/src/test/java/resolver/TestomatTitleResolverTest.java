package resolver;

import io.testomat.core.annotation.Title;
import io.testomat.resolver.TestomatTitleResolver;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TestomatTitleResolverTest {

    TestomatTitleResolver resolver = new TestomatTitleResolver();

    static class TestClass {

        @Title("Custom title")
        void testWithTitle() {}

        void testWithoutTitle() {}

    }

    @Test
    void shouldResolveTitle() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithTitle");
        String result = resolver.resolve(method);
        assertThat(result).isEqualTo("Custom title");
    }

    @Test
    void shouldReturnNullIfNoAnnotation() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithoutTitle");
        String result = resolver.resolve(method);
        assertThat(result).isNull();
    }

}
