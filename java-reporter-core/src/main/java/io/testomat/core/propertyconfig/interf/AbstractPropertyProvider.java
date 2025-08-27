package io.testomat.core.propertyconfig.interf;


import io.testomat.core.propertyconfig.provider.DefaultPropertyProvider;
import io.testomat.core.propertyconfig.provider.FilePropertyProvider;
import io.testomat.core.propertyconfig.provider.JvmSystemPropertyProvider;
import io.testomat.core.propertyconfig.provider.SystemEnvPropertyProvider;

/**
 * Abstract base class for property providers implementing chain of responsibility pattern.
 * Provides common chain management functionality and defines the standard Testomat.io
 * property resolution order with factory method for consistent provider setup.
 * 
 * <p>The standard chain order prioritizes more specific/temporary sources over general ones:
 * <ol>
 *   <li>JVM System Properties (-Dtestomatio.api.key=value)</li>
 *   <li>Environment Variables (TESTOMATIO_API_KEY=value)</li> 
 *   <li>Property Files (testomatio.properties)</li>
 *   <li>Default Values (built-in fallbacks)</li>
 * </ol>
 */
public abstract class AbstractPropertyProvider implements PropertyProvider {
    protected PropertyProvider next;

    /**
     * Creates array of property providers in standard Testomat.io priority order.
     * This defines the canonical property resolution chain used throughout the library.
     * 
     * <p>Resolution order (highest to lowest priority):
     * <ol>
     *   <li>{@link JvmSystemPropertyProvider} - JVM system properties (-D flags)</li>
     *   <li>{@link SystemEnvPropertyProvider} - Environment variables</li>
     *   <li>{@link FilePropertyProvider} - Properties files (testomatio.properties)</li>
     *   <li>{@link DefaultPropertyProvider} - Built-in default values</li>
     * </ol>
     *
     * @return array of property providers in resolution priority order
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
