package io.testomat.core.propertyconfig.impl;

import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;

/**
 * Singleton factory implementation creating standard Testomat.io property provider chains.
 * Assembles the complete chain of responsibility for property resolution with proper
 * fallback behavior according to priority order.
 * 
 * <p>Creates pre-configured chain: JVM properties → Environment → File → Defaults.
 * This ensures consistent property resolution behavior across all Testomat.io components.
 */
public class PropertyProviderFactoryImpl implements PropertyProviderFactory {
    private static PropertyProviderFactory instance;

    private PropertyProviderFactoryImpl() {
    }

    /**
     * Returns the singleton factory instance with thread-safe lazy initialization.
     * Multiple calls return the same instance, ensuring consistent property provider
     * configuration across the application.
     *
     * @return PropertyProviderFactory singleton instance
     */
    public static PropertyProviderFactory getPropertyProviderFactory() {
        if (instance == null) {
            instance = new PropertyProviderFactoryImpl();
        }
        return instance;
    }

    /**
     * Creates a fully configured property provider chain ready for use.
     * Links all providers in the standard priority order and returns the chain head.
     * 
     * @return head of the property provider chain (JvmSystemPropertyProvider)
     */
    @Override
    public PropertyProvider getPropertyProvider() {
        PropertyProvider[] chain = AbstractPropertyProvider.getPropertyProviders();

        for (int i = 0; i < chain.length - 1; i++) {
            chain[i].setNext(chain[i + 1]);
        }

        return chain[0];
    }
}
