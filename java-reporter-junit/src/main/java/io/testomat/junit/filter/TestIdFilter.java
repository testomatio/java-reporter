package io.testomat.junit.filter;

import io.testomat.core.annotation.TestId;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
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

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            TestMethodTestDescriptor methodDescriptor = (TestMethodTestDescriptor) testDescriptor;
            Method testMethod = methodDescriptor.getTestMethod();

            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);

            if (testIdAnnotation != null) {
                return getFilterResult(testIdAnnotation);
            } else {
                return FilterResult.included(
                "Test method without @TestId annotation is allowed when filtering by IDs");
            }
        }

        return FilterResult.included("Not a test method");
    }

    private FilterResult getFilterResult(TestId testIdAnnotation) {
        String testId = testIdAnnotation.value();
        boolean isAllowed = allowedIds.contains(testId);

        if (isAllowed) {
            return FilterResult.included("Test ID " + testId + " is in allowed list");
        } else {
            return FilterResult.excluded("Test ID " + testId + " is not in allowed list");
        }
    }
}
