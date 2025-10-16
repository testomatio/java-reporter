package io.testomat.core.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaStorage {
    public static ThreadLocal<Map<String, String>> TEMP_META_STORAGE =
            ThreadLocal.withInitial(HashMap::new);

    public static volatile Map<String, Map<String, String>> LINKED_META_STORAGE =
            new ConcurrentHashMap<>();
}
