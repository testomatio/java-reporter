package io.testomat.core.client.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeObjectNormalizer {

    private static final Logger log = LoggerFactory.getLogger(SafeObjectNormalizer.class);

    private final ObjectMapper objectMapper;

    public SafeObjectNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object normalize(Object value) {
        Set<Object> activeObjects = Collections.newSetFromMap(new IdentityHashMap<>());

        return normalizeValue(value, activeObjects);
    }

    private Object normalizeValue(Object value, Set<Object> activeObjects) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?>) {
            if (!activeObjects.add(value)) {
                return null;
            }

            try {
                Map<String, Object> normalizedMap = new LinkedHashMap<>();

                ((Map<?, ?>) value).forEach((key, val) ->
                        normalizedMap.put(String.valueOf(key), normalizeValue(val, activeObjects)));

                return normalizedMap;
            } finally {
                activeObjects.remove(value);
            }
        }

        if (value instanceof Collection<?>) {
            if (!activeObjects.add(value)) {
                return null;
            }

            try {
                List<Object> normalizedList = new ArrayList<>();

                for (Object item : (Collection<?>) value) {
                    normalizedList.add(normalizeValue(item, activeObjects));
                }

                return normalizedList;
            } finally {
                activeObjects.remove(value);
            }
        }

        if (value.getClass().isArray()) {
            if (!activeObjects.add(value)) {
                return null;
            }

            try {
                List<Object> normalizedList = new ArrayList<>();
                int length = Array.getLength(value);

                for (int i = 0; i < length; i++) {
                    normalizedList.add(normalizeValue(Array.get(value, i), activeObjects));
                }

                return normalizedList;
            } finally {
                activeObjects.remove(value);
            }
        }

        try {
            objectMapper.writeValueAsString(value);
            return value;
        } catch (JsonProcessingException e) {
            log.warn("Unable to serialize value of type: {}. Converting to string.",
                    value.getClass().getName(),
                    e);

            return String.valueOf(value);
        }
    }
}
