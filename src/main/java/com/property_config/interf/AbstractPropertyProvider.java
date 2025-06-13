package com.property_config.interf;


import com.property_config.provider.DefaultPropertyProvider;
import com.property_config.provider.JvmSystemPropertyProvider;
import com.property_config.provider.SystemEnvPropertyProvider;

public abstract class AbstractPropertyProvider implements PropertyProvider {
    protected PropertyProvider next;

    public static PropertyProvider[] getPropertyProviders() {
        return new PropertyProvider[]{
                new JvmSystemPropertyProvider(),
                new SystemEnvPropertyProvider(),
                new DefaultPropertyProvider()
        };
    }

    @Override
    public void setNext(PropertyProvider next) {
        this.next = next;
    }
}
