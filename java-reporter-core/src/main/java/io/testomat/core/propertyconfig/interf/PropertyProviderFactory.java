package io.testomat.core.propertyconfig.interf;

/**
 * Factory interface for creating pre-configured property provider chains.
 * Implementations define the specific chain order and initialization logic
 * for different property resolution strategies.
 */
public interface PropertyProviderFactory {

    /**
     * Creates a property provider with a pre-configured chain of responsibility.
     * The returned provider is the head of the chain and will delegate through
     * all configured providers until a property is found or all sources are exhausted.
     *
     * @return property provider chain head, ready for property resolution
     */
    PropertyProvider getPropertyProvider();
}
