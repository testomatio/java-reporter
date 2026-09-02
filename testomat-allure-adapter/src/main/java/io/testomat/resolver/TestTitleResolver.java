package io.testomat.resolver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Resolves test title using registered TestMetadataResolvers.
 */
public class TestTitleResolver {

    private static List<TestMetadataResolver> RESOLVERS = loadResolvers();

    public static void setResolvers(List<TestMetadataResolver> resolvers) {
        RESOLVERS = resolvers;
    }

    /** Loads resolvers via ServiceLoader. */
    private static List<TestMetadataResolver> loadResolvers() {
        List<TestMetadataResolver> list = new ArrayList<>();
        ServiceLoader<TestMetadataResolver> loader = ServiceLoader.load(TestMetadataResolver.class);
        loader.forEach(list::add);

        return list;
    }

    /**
     * Resolves test title using available resolvers.
     * @param method test method
     * @return resolved title or method name if not found
     */
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
