package com.testomatio.reporter.property_config.interf;

/**
 * Property provider interface supporting chain of responsibility pattern.
 * Allows searching for properties across multiple sources with fallback behavior.
 */
public interface PropertyProvider {

    /**
     * Gets property value by key.
     *
     * @param key property key
     * @return property value
     * @throws com.testomatio.reporter.exception.PropertyNotFoundException if property not found
     */
    String getProperty(String key);

    /**
     * Sets next provider in the chain for fallback.
     *
     * @param next next property provider
     */
    void setNext(PropertyProvider next);
}