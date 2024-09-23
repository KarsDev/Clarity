package me.kuwg.clarity.info;

import java.util.HashMap;
import java.util.Map;

public final class ClarityRunningInformation {

    private static final Option[] OPTIONS = {
            load("optimize", true, "noopt", "opt"),


    };

    private final Map<String, Boolean> loadedOptions;

    public ClarityRunningInformation(final String[] args) {
        loadedOptions = new HashMap<>();

        for (String arg : args) {
            for (Option option : OPTIONS) {
                if (arg.equals(option.argTrue)) {
                    loadedOptions.put(option.name, true);
                } else if (arg.equals(option.argFalse)) {
                    loadedOptions.put(option.name, false);
                }
            }
        }

        for (Option option : OPTIONS) {
            loadedOptions.putIfAbsent(option.name, option.def);
        }
    }

    private static Option load(final String name, final boolean def, final String argFalse, final String argTrue) {
        return new Option(name, def, argFalse, argTrue);
    }

    public boolean getOption(String name) {
        return loadedOptions.getOrDefault(name, false);
    }

    private static class Option {
        public final String name;
        public final boolean def;
        public final String argFalse;
        public final String argTrue;

        private Option(final String name, final boolean def, final String argFalse, final String argTrue) {
            this.name = name;
            this.def = def;
            this.argFalse = "-" + argFalse;
            this.argTrue = "-" + argTrue;
        }
    }
}
