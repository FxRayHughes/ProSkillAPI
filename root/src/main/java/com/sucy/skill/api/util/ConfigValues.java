package com.sucy.skill.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Normalizes YAML strings and JSON primitive values at configuration boundaries. */
public final class ConfigValues {
    private ConfigValues() { }

    public static List<String> strings(List<?> values) {
        if (values == null || values.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>(values.size());
        for (Object value : values) if (value != null) result.add(String.valueOf(value));
        return result;
    }

    public static String string(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }
}
