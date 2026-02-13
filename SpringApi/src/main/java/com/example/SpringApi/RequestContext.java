package com.example.SpringApi;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
    private RequestContext() {
    }

    private static final ThreadLocal<Map<String, String>> context = ThreadLocal.withInitial(HashMap::new);

    // Set data into the context
    public static void set(String key, String value) {
        context.get().put(key, value);
    }

    // Get data from the context
    public static String get(String key) {
        return context.get().get(key);
    }

    // Clear the context after use
    public static void clear() {
        context.remove();
    }

    // Optionally, you can also expose all data at once if needed
    public static Map<String, String> getAll() {
        return context.get();
    }
}
