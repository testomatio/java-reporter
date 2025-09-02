package io.testomat.testng.filter;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;

public class FilterRegistrator {
    private static final Logger log = LoggerFactory.getLogger(FilterRegistrator.class);

    private final List<CustomSelector> customSelectors = new ArrayList<>();

    public FilterRegistrator() {
        System.out.println("DEBUG: FilterRegistrator constructor called");
        customSelectors.add(new TestIdSelector());
    }

    public void registerFilter(ISuite suite, String filterPropertyName,
                               String filterClassName, int priority) {

        if (suite == null || filterPropertyName == null || filterClassName == null) {
            log.warn("Cannot register filter: invalid parameters");
            return;
        }

        String providedIds = System.getProperty(filterPropertyName);
        System.out.println("DEBUG: FilterRegistrator: checking property '" + filterPropertyName + "' = '" + providedIds + "' for filter '" + filterClassName + "'");

        if (providedIds != null) {
            XmlSuite xmlSuite = suite.getXmlSuite();
            System.out.println("DEBUG: XmlSuite found: " + xmlSuite.getName());

            XmlMethodSelector selector = new XmlMethodSelector();
            selector.setClassName(filterClassName);
            selector.setPriority(priority);
            System.out.println("DEBUG: Created XmlMethodSelector with class: " + filterClassName + ", priority: " + priority);

            xmlSuite.getMethodSelectors().add(selector);
            System.out.println("DEBUG: Filter " + filterClassName + " registered successfully for property " + filterPropertyName + " with value: " + providedIds);
            System.out.println("DEBUG: Total method selectors in suite: " + xmlSuite.getMethodSelectors().size());
            log.debug("Filter {} registered for property {} with value: {}",
                    filterClassName, filterPropertyName, providedIds);
        } else {
            System.out.println("DEBUG: Property '" + filterPropertyName + "' is null - filter NOT registered");
        }
    }

    public void registerAllFilters(ISuite suite) {
        System.out.println("DEBUG: registerAllFilters called with " + customSelectors.size() + " selectors");
        for (CustomSelector selector : customSelectors) {
            System.out.println("DEBUG: Registering selector: " + selector.getFilterClassName());
            registerFilter(suite,
                    selector.getFilterPropertyName(),
                    selector.getFilterClassName(),
                    selector.getPriority());
        }
        System.out.println("DEBUG: registerAllFilters completed");
    }
}
