package io.testomat.core.propertyconfig.interf;


import io.testomat.core.propertyconfig.provider.DefaultPropertyProvider;
import io.testomat.core.propertyconfig.provider.FilePropertyProvider;
import io.testomat.core.propertyconfig.provider.JvmSystemPropertyProvider;
import io.testomat.core.propertyconfig.provider.SystemEnvPropertyProvider;

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
