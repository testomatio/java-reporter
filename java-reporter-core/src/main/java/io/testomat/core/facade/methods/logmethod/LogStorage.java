package io.testomat.core.facade.methods.logmethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogStorage {
    private static final LogStorage INSTANCE = new LogStorage();

    private final ThreadLocal<List<String>> TEMP_LOG_STORAGE = ThreadLocal.withInitial(ArrayList::new);

    private final Map<String, String[]> LINKED_LOG_STORAGE = new ConcurrentHashMap<>();

    private LogStorage() {
    }

    public static LogStorage getInstance() {
        return INSTANCE;
    }

    public ThreadLocal<List<String>> getTempLogStorage() {
        return TEMP_LOG_STORAGE;
    }

    public Map<String, String[]> getLinkedLogStorage() {
        return LINKED_LOG_STORAGE;
    }
}
