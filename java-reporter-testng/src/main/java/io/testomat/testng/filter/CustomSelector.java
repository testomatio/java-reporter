package io.testomat.testng.filter;

import org.testng.IMethodSelector;

public interface CustomSelector extends IMethodSelector {
    String getFilterPropertyName();

    String getFilterClassName();

    int getPriority();
}
