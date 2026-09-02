package io.testomat.core.facade.methods.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaStorage {
    
    private static final ThreadLocal<Map<String, String>> TEMP_META_STORAGE =
            ThreadLocal.withInitial(HashMap::new);

    private static final Map<String, Map<String, String>> LINKED_META_STORAGE =
            new ConcurrentHashMap<>();

    public static Map<String, Map<String, String>> getLinkedMetaStorage() {
        return LINKED_META_STORAGE;
    }

    public static void clearLinkedMetaStorage() {
        LINKED_META_STORAGE.clear();
    }

    public static Map<String, String> getTempMetaStorage() {
        return TEMP_META_STORAGE.get();
    }

    public static void clearTempMetaStorage() {
        TEMP_META_STORAGE.remove();
    }
}
