package io.testomat.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves TestNG test description from @Test annotation.
 */
public class TestNgResolver implements TestMetadataResolver {
    private static final Logger log = LoggerFactory.getLogger(TestNgResolver.class);

    /**
     * Extracts test description via reflection.
     * @param method test method
     * @return description or null if not present
     */
    @Override
    public String resolve(Method method) {
        for (Annotation a : method.getAnnotations()) {
            if (a.annotationType().getName().equals("org.testng.annotations.Test")) {
                try {
                    Method m = a.annotationType().getMethod("description");
                    String value = (String) m.invoke(a);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                } catch (Exception ignored) {
                    log.trace("Failed to resolve TestNG description for {}", method.getName());
                }
            }
        }
        return null;
    }
}
