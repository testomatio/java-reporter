package io.testomat.resolver;

import java.lang.reflect.Method;

public interface TestMetadataResolver {
    String resolve(Method method);
}