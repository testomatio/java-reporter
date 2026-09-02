package io.testomat.core.facade.methods.label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LabelStorage {
    private static final ThreadLocal<List<Map<String, String>>> TEMP_LABEL_STORAGE =
            ThreadLocal.withInitial(ArrayList::new);

    private static final Map<String, List<Map<String, String>>> LINKED_LABEL_STORAGE =
            new ConcurrentHashMap<>();

    public static List<Map<String, String>> getTempLabelStorage() {
        return TEMP_LABEL_STORAGE.get();
    }

    public static void clearTempLabelStorage() {
        TEMP_LABEL_STORAGE.remove();
    }

    public static Map<String, List<Map<String, String>>> getLinkedLabelStorage() {
        return LINKED_LABEL_STORAGE;
    }

    public static void clearLinkedLabelStorage() {
        LINKED_LABEL_STORAGE.clear();
    }
}
