package io.testomat.core.propertyconfig.impl;

import io.testomat.core.propertyconfig.interf.AbstractPropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProvider;
import io.testomat.core.propertyconfig.interf.PropertyProviderFactory;

/**
 * Singleton factory creating property provider chains with fallback behavior.
 * Configures chain order: JVM properties → Environment → File → Defaults.
 */
public class PropertyProviderFactoryImpl implements PropertyProviderFactory {
    private static PropertyProviderFactory instance;

    private PropertyProviderFactoryImpl() {
    }

    /**
     * Returns singleton factory instance.
     *
     * @return PropertyProviderFactory instance
     */
    public static PropertyProviderFactory getPropertyProviderFactory() {
        if (instance == null) {
            instance = new PropertyProviderFactoryImpl();
        }
        return instance;
    }

    @Override
    public PropertyProvider getPropertyProvider() {
        PropertyProvider[] chain = AbstractPropertyProvider.getPropertyProviders();

        for (int i = 0; i < chain.length - 1; i++) {
            chain[i].setNext(chain[i + 1]);
        }

        return chain[0];
    }
}
