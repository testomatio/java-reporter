package io.testomat.core.facade.methods.label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LabelStorage {
    public static ThreadLocal<List<Map<String, String>>> TEMP_LABEL_STORAGE =
            ThreadLocal.withInitial(ArrayList::new);

    public static volatile Map<String, List<Map<String, String>>> LINKED_LABEL_STORAGE =
            new ConcurrentHashMap<>();
}
