package com.testomatio.reporter.property_config.impl;

import com.testomatio.reporter.property_config.interf.AbstractPropertyProvider;
import com.testomatio.reporter.property_config.interf.PropertyProvider;
import com.testomatio.reporter.property_config.interf.PropertyProviderFactory;

public class PropertyProviderFactoryImpl implements PropertyProviderFactory {
    private static PropertyProviderFactory instance;
    private PropertyProviderFactoryImpl() {
    }

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
