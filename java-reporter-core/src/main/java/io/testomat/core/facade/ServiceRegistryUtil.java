package io.testomat.core.facade;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service registry utility for managing singleton service instances.
 * Provides thread-safe lazy initialization of services using reflection.
 */
public class ServiceRegistryUtil {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    
    /**
     * Gets or creates a singleton instance of the specified service class.
     *
     * @param serviceClass the class of the service to retrieve
     * @param <T> the type of the service
     * @return singleton instance of the service
     * @throws RuntimeException if service cannot be instantiated
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        return (T) services.computeIfAbsent(serviceClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create service: " + clazz, e);
            }
        });
    }
}
