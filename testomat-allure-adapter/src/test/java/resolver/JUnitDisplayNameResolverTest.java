package resolver;

import io.testomat.resolver.JUnitDisplayNameResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.assertThat;

class JUnitDisplayNameResolverTest {

    JUnitDisplayNameResolver resolver = new JUnitDisplayNameResolver();

    static class TestClass {

        @DisplayName("My test title")
        void testWithDisplayName() {}

        @DisplayName("")
        void testWithEmptyDisplayName() {}

        void testWithoutDisplayName() {}

    }

    @Test
    void shouldResolveDisplayName() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithDisplayName");
        String result = resolver.resolve(method);
        assertThat(result).isEqualTo("My test title");
    }

    @Test
    void shouldReturnNullIfDisplayNameEmpty() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithEmptyDisplayName");
        String result = resolver.resolve(method);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullIfNoDisplayName() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("testWithoutDisplayName");
        String result = resolver.resolve(method);
        assertThat(result).isNull();
    }

}
