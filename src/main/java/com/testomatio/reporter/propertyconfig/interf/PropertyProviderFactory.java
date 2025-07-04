package com.testomatio.reporter.propertyconfig.interf;

/**
 * Factory for creating configured property provider chains.
 */
public interface PropertyProviderFactory {

    /**
     * Creates property provider with configured chain of responsibility.
     *
     * @return property provider chain head
     */
    PropertyProvider getPropertyProvider();
}
