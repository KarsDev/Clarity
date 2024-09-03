package me.kuwg.clarity.debug;

import java.util.*;

public class MethodTimingRegistry {
    private static final Map<String, Long> methodTimings = new HashMap<>();

    public static void register(final String methodName, final long duration) {
        methodTimings.merge(methodName, duration, Long::sum);
    }

    public static List<Map.Entry<String, Long>> getSortedTimings() {
        List<Map.Entry<String, Long>> entries = new ArrayList<>(methodTimings.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        return entries;
    }
}
