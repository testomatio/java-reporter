package com.testomatio.reporter.property_config.interf;

import com.testomatio.reporter.property_config.provider.DefaultPropertyProvider;
import com.testomatio.reporter.property_config.provider.FilePropertyProvider;
import com.testomatio.reporter.property_config.provider.JvmSystemPropertyProvider;
import com.testomatio.reporter.property_config.provider.SystemEnvPropertyProvider;

public abstract class AbstractPropertyProvider implements PropertyProvider {
    protected PropertyProvider next;

    public static PropertyProvider[] getPropertyProviders() {
        return new PropertyProvider[]{
                new JvmSystemPropertyProvider(),
                new SystemEnvPropertyProvider(),
                new FilePropertyProvider(),
                new DefaultPropertyProvider()
        };
    }

    @Override
    public void setNext(PropertyProvider next) {
        this.next = next;
    }
}
