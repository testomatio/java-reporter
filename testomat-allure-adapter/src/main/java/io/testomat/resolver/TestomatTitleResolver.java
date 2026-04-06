package io.testomat.resolver;

import io.testomat.core.annotation.Title;
import java.lang.reflect.Method;

public class TestomatTitleResolver implements TestMetadataResolver {

    @Override
    public String resolve(Method method) {
        Title title = method.getAnnotation(Title.class);
        if (title != null) {
            return title.value();
        }

        return null;
    }
}
