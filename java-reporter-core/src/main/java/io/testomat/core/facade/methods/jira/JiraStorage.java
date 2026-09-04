package io.testomat.core.facade.methods.jira;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JiraStorage {

    private static final ThreadLocal<List<String>> TEMP_JIRA_LINK_STORAGE =
            ThreadLocal.withInitial(ArrayList::new);

    private static final Map<String, List<String>> LINKED_JIRA_LINK_STORAGE =
            new ConcurrentHashMap<>();

    public static Map<String, List<String>> getLinkedJiraStorage() {
        return LINKED_JIRA_LINK_STORAGE;
    }

    public static List<String> getTempJiraStorage() {
        return TEMP_JIRA_LINK_STORAGE.get();
    }

    public static void clearTempJiraStorage() {
        TEMP_JIRA_LINK_STORAGE.remove();
    }

    public static void clearLinkedJiraStorage() {
        LINKED_JIRA_LINK_STORAGE.clear();
    }

}
