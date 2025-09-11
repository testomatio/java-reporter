package io.testomat.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    
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