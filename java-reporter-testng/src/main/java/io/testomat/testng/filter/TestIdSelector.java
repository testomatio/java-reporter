package io.testomat.testng.filter;

import io.testomat.core.annotation.TestId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class TestIdSelector implements IMethodSelector, CustomSelector {
    private static final Logger log = LoggerFactory.getLogger(TestIdSelector.class);
    private static final String SPLIT_REGEX = ",";
    private final String filterClassName = "io.testomat.testng.filter.TestIdSelector";
    private final String filterPropertyName = "ids";
    private final int priority = 1;
    
    private Set<String> cachedTestIds = null;
    private String lastProvidedIds = null;

    public TestIdSelector() {
        log.debug("TestIdSelector created");
    }

    @Override
    public boolean includeMethod(IMethodSelectorContext context,
                                 ITestNGMethod method,
                                 boolean isTestMethod) {
        try {
            String providedIds = System.getProperty(filterPropertyName);
            log.debug("Checking method '{}' with property '{}' = '{}'", 
                     method.getMethodName(), filterPropertyName, providedIds);

            if (providedIds == null || providedIds.trim().isEmpty()) {
                log.debug("No filtering applied for method: {}", method.getMethodName());
                return true;
            }

            Set<String> testIds = getTestIds(providedIds);

            TestId annotation = method.getConstructorOrMethod()
                    .getMethod()
                    .getAnnotation(TestId.class);

            boolean result = annotation != null && testIds.contains(annotation.value());
            log.debug("Method '{}', annotation: {}, testIds: {}, result: {}", 
                     method.getMethodName(), 
                     annotation != null ? annotation.value() : "null", 
                     testIds, result);
            return result;
        } catch (Exception e) {
            log.error("Error in TestIdSelector for method: {}", method.getMethodName(), e);
            return true; // fallback to include method
        }
    }

    private Set<String> getTestIds(String providedIds) {
        if (cachedTestIds == null || !providedIds.equals(lastProvidedIds)) {
            cachedTestIds = Arrays.stream(providedIds.split(SPLIT_REGEX))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            lastProvidedIds = providedIds;
            log.debug("Cached test IDs updated: {}", cachedTestIds);
        }
        return cachedTestIds;
    }

    @Override
    public void setTestMethods(List<ITestNGMethod> list) {
        log.debug("TestIdSelector.setTestMethods called with {} methods", 
                 list != null ? list.size() : "null");
    }

    @Override
    public String getFilterPropertyName() {
        return filterPropertyName;
    }

    @Override
    public String getFilterClassName() {
        return filterClassName;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
