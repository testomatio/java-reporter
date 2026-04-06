package io.testomat.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class TestTitleResolver {

    private static List<TestMetadataResolver> RESOLVERS = loadResolvers();

    public static void setResolvers(List<TestMetadataResolver> resolvers) {
        RESOLVERS = resolvers;
    }

    private static List<TestMetadataResolver> loadResolvers() {
        List<TestMetadataResolver> list = new ArrayList<>();
        ServiceLoader<TestMetadataResolver> loader = ServiceLoader.load(TestMetadataResolver.class);
        loader.forEach(list::add);

        return list;
    }

    public static String resolve(Method method) {
        for (TestMetadataResolver resolver : RESOLVERS) {
            String title = resolver.resolve(method);
            if (title != null) {
                return title;
            }
        }

        return method.getName();
    }

}
