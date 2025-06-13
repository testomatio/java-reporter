package com.property_config.interf;

public interface PropertyProvider {
    String getProperty(String key);

    void setNext(PropertyProvider next);
}
