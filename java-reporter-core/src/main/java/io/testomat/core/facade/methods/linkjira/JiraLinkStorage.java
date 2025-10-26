package io.testomat.core.facade.methods.linkjira;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JiraLinkStorage {
    public static ThreadLocal<List<String>> TEMP_JIRA_LINK_STORAGE =
            ThreadLocal.withInitial(ArrayList::new);

    public static volatile Map<String, List<String>> LINKED_JIRA_LINK_STORAGE =
            new ConcurrentHashMap<>();
}
