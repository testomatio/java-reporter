package io.testomat.core.facade.methods.logmethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogStorage {

    public static final ThreadLocal<List<String>> TEMP_LOG_STORAGE =
            ThreadLocal.withInitial(ArrayList::new);

    public static final Map<String, String[]> LINKED_LOG_STORAGE = new ConcurrentHashMap<>();
}
