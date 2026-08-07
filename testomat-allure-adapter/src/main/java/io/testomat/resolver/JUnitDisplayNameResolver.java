package io.testomat.resolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves test display name from JUnit @DisplayName annotation.
 */
public class JUnitDisplayNameResolver implements TestMetadataResolver {
    private static final Logger log = LoggerFactory.getLogger(JUnitDisplayNameResolver.class);

    /**
     * Extracts display name from method annotations.
     * @param method test method
     * @return display name or null if not present
     */
    @Override
    public String resolve(Method method) {
        for (Annotation ann : method.getAnnotations()) {
            if (ann.annotationType().getName().equals("org.junit.jupiter.api.DisplayName")) {
                try {
                    Method m = ann.annotationType().getMethod("value");
                    String value = (String) m.invoke(ann);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                } catch (Exception ignored) {
                    log.debug("Failed to resolve JUnit DisplayName");
                }
            }
        }

        return null;
    }
}
