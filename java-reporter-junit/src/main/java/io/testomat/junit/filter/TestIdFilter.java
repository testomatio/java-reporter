package io.testomat.junit.filter;

import io.testomat.core.annotation.TestId;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;

public class TestIdFilter implements PostDiscoveryFilter {
    private static final String SPLIT_REGEX = ",";
    private static final String IDS_PROPERTY_NAME = "ids";

    private final Set<String> allowedIds;

    public TestIdFilter() {
        String idsProperty = System.getProperty(IDS_PROPERTY_NAME);

        if (idsProperty != null && !idsProperty.isEmpty()) {
            allowedIds = new HashSet<>();
            for (String id : idsProperty.split(SPLIT_REGEX)) {
                allowedIds.add(id.trim());
            }
        } else {
            allowedIds = null;
        }
    }

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        if (allowedIds == null) {
            return FilterResult.included("No ids filter specified");
        }

        Optional<TestSource> optionalTestSource = testDescriptor.getSource();

        if (optionalTestSource.isEmpty()) {
            return FilterResult.included("No source specified");
        }

        TestSource source = optionalTestSource.get();

        if (!(source instanceof MethodSource)) {
            return FilterResult.included("No method specified");
        }

        return resolve((MethodSource) source);
    }

    private FilterResult resolve(MethodSource methodSource) {

        Method method = methodSource.getJavaMethod();
        if (method == null) {
            return FilterResult.included("No Java method");
        }

        TestId testId = method.getAnnotation(TestId.class);

        if (testId == null) {
            return FilterResult.excluded("No TestId annotation found");
        }

        return allowedIds.contains(testId.value())
            ? FilterResult.included("Allowed " + testId.value())
            : FilterResult.excluded("Not allowed " + testId.value());
    }
}
