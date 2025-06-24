package com.testomatio.reporter.property_config.interf;

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