package io.testomat.junit;

import io.testomat.core.annotation.TestId;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.PostDiscoveryFilter;

public class TestIdFilterWithLogging implements PostDiscoveryFilter {

    private static int filterCallCount = 0;
    private final Set<String> allowedIds;

    public TestIdFilterWithLogging() {
        String idsProperty = System.getProperty("ids");
        System.out.println("=== ФІЛЬТР ІНІЦІАЛІЗУЄТЬСЯ ===");
        System.out.println("System property 'ids': " + idsProperty);

        if (idsProperty != null && !idsProperty.isEmpty()) {
            allowedIds = new HashSet<>();
            for (String id : idsProperty.split(",")) {
                allowedIds.add(id.trim());
            }
            System.out.println("Allowed IDs: " + allowedIds);
        } else {
            allowedIds = null;
            System.out.println("No filtering - all tests will run");
        }
        System.out.println("=== ФІЛЬТР ГОТОВИЙ ===");
    }

    @Override
    public FilterResult apply(TestDescriptor testDescriptor) {
        filterCallCount++;
        String descriptorType = testDescriptor.getClass().getSimpleName();
        String displayName = testDescriptor.getDisplayName();

        System.out.println(String.format("Filter call #%d: %s - %s",
                filterCallCount, descriptorType, displayName));

        if (allowedIds == null) {
            System.out.println("  -> INCLUDED (no filter)");
            return FilterResult.included("No ids filter specified");
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            TestMethodTestDescriptor methodDescriptor = (TestMethodTestDescriptor) testDescriptor;
            Method testMethod = methodDescriptor.getTestMethod();

            TestId testIdAnnotation = testMethod.getAnnotation(TestId.class);

            if (testIdAnnotation != null) {
                String testId = testIdAnnotation.value();
                boolean isAllowed = allowedIds.contains(testId);

                System.out.println(String.format("  -> Test ID: %s, Allowed: %s",
                        testId, isAllowed ? "YES" : "NO"));

                if (isAllowed) {
                    return FilterResult.included("Test ID " + testId + " is in allowed list");
                } else {
                    return FilterResult.excluded("Test ID " + testId + " is not in allowed list");
                }
            } else {
                System.out.println("  -> EXCLUDED (no @TestId annotation)");
                return FilterResult.excluded("Test method does not have @TestId annotation");
            }
        }

        System.out.println("  -> INCLUDED (not a test method)");
        return FilterResult.included("Not a test method");
    }
}
