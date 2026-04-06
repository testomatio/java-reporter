package io.testomat.resolver;

import java.lang.reflect.Method;

public class TestNgResolver implements TestMetadataResolver {

    @Override
    public String resolve(Method method) {
        for (var a : method.getAnnotations()) {
            if (a.annotationType().getName().equals("org.testng.annotations.Test")) {
                try {
                    Method m = a.annotationType().getMethod("description");
                    String value = (String) m.invoke(a);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
                catch (Exception ignored) {}
            }
        }
        return null;
    }
}
