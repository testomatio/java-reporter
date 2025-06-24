package com.testomatio.reporter.property_config.interf;

import com.testomatio.reporter.property_config.provider.DefaultPropertyProvider;
import com.testomatio.reporter.property_config.provider.FilePropertyProvider;
import com.testomatio.reporter.property_config.provider.JvmSystemPropertyProvider;
import com.testomatio.reporter.property_config.provider.SystemEnvPropertyProvider;

/**
 * Base class for property providers implementing chain of responsibility pattern.
 * Defines the standard property provider chain order and provides factory method.
 */
public abstract class AbstractPropertyProvider implements PropertyProvider {
    protected PropertyProvider next;

    /**
     * Creates array of property providers in priority order:
     * JVM system properties → Environment variables → File properties → Defaults.
     *
     * @return array of property providers in chain order
     */
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