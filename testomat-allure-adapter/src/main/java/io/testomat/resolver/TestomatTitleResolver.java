package io.testomat.resolver;

import io.testomat.core.annotation.Title;
import java.lang.reflect.Method;

/**
 * Resolves test title from @Title annotation.
 */
public class TestomatTitleResolver implements TestMetadataResolver {

    /**
     * @param method test method
     * @return title value or null if not present
     */
    @Override
    public String resolve(Method method) {
        Title title = method.getAnnotation(Title.class);
        if (title != null) {
            return title.value();
        }

        return null;
    }
}
