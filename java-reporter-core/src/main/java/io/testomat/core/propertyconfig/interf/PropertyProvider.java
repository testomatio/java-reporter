package io.testomat.core.propertyconfig.interf;

import io.testomat.core.exception.PropertyNotFoundException;

/**
 * Property provider interface supporting chain of responsibility pattern.
 * Allows searching for properties across multiple sources with fallback behavior.
 * 
 * <p>Implementations search for properties in their specific source (JVM properties,
 * environment variables, files, etc.) and delegate to the next provider in the chain
 * if the property is not found.
 * 
 * <p>Example usage:
 * <pre>{@code
 * PropertyProvider provider = new JvmSystemPropertyProvider();
 * provider.setNext(new SystemEnvPropertyProvider());
 * String apiKey = provider.getProperty("testomatio.api.key");
 * }</pre>
 */
public interface PropertyProvider {

    /**
     * Retrieves property value by key from this provider or delegates to next in chain.
     * Searches this provider's source first, then delegates to next provider if not found.
     *
     * @param key property key to search for (e.g., "testomatio.api.key")
     * @return property value if found
     * @throws PropertyNotFoundException if property not found in this provider or any chained providers
     */
    String getProperty(String key);

    /**
     * Sets the next provider in the chain for fallback property resolution.
     * When this provider cannot find a property, it delegates to the next provider.
     *
     * @param next next property provider in the chain, or null if this is the last provider
     */
    void setNext(PropertyProvider next);
}
