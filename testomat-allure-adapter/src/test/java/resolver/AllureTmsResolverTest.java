package resolver;

import io.qameta.allure.TmsLink;
import io.testomat.resolver.AllureTmsResolver;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AllureTmsResolverTest {

    private AllureTmsResolver resolver;

    @BeforeEach
    void setup() {
        resolver = new AllureTmsResolver();
    }

    static class TestClass {

        @TmsLink("TMS-123")
        void methodWithTmsLink() {
        }

        void methodWithoutTmsLink() {
        }
    }

    @Test
    void shouldReturnTmsLinkValue() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("methodWithTmsLink");

        String result = resolver.resolve(method);

        assertThat(result).isEqualTo("TMS-123");
    }

    @Test
    void shouldReturnNullWhenTmsLinkIsAbsent() throws Exception {
        Method method = TestClass.class.getDeclaredMethod("methodWithoutTmsLink");

        String result = resolver.resolve(method);

        assertThat(result).isNull();
    }
}