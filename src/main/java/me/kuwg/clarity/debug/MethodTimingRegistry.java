package me.kuwg.clarity.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodTimingRegistry {
    private static final Map<String, Long> methodTimings = new HashMap<>();

    public static void register(final String methodName, final long duration) {
        methodTimings.put(methodName, methodTimings.getOrDefault(methodName, 0L) + duration);
    }

    public static List<Map.Entry<String, Long>> getSortedTimings() {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(methodTimings.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        return entries;
    }
}