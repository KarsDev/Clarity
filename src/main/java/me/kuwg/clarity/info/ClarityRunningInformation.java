package me.kuwg.clarity.info;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ClarityRunningInformation} class is responsible for managing and loading
 * configuration options from the command-line arguments. It interprets specific arguments
 * and determines their boolean values based on predefined options.
 */
public final class ClarityRunningInformation {

    /**
     * Array of options available for configuration.
     * Each option consists of a name, a default boolean value, and its corresponding
     * true and false argument representations.
     */
    private static final Option[] OPTIONS = {
            load("optimize", true, "noopt", "opt"),       // optimization option
            load("verbose", false, "noverb", "verb"),     // verbose option
            load("startinfo", false, "nsinfo", "sinfo"),  // starting info

    };

    /**
     * A map that holds the loaded options from the command-line arguments.
     * The key is the option name, and the value is its boolean status.
     */
    private final Map<String, Boolean> loadedOptions;

    /**
     * Constructor that initializes the {@code ClarityRunningInformation} object
     * by parsing command-line arguments and loading the corresponding options.
     *
     * @param args an array of command-line arguments passed to the program
     */
    public ClarityRunningInformation(final String[] args) {
        loadedOptions = new HashMap<>();

        // Parse the arguments and load options based on true/false values
        for (final String arg : args) {
            for (final Option option : OPTIONS) {
                if (arg.equals(option.argTrue)) {
                    loadedOptions.put(option.name, true);
                } else if (arg.equals(option.argFalse)) {
                    loadedOptions.put(option.name, false);
                }
            }
        }

        // Set the default value for options that were not provided in the arguments
        for (final Option option : OPTIONS) {
            loadedOptions.putIfAbsent(option.name, option.def);
        }
    }

    /**
     * Creates and returns a new {@code Option} object with the given parameters.
     *
     * @param name     the name of the option
     * @param def      the default boolean value for the option
     * @param argFalse the argument that corresponds to the "false" value of the option
     * @param argTrue  the argument that corresponds to the "true" value of the option
     * @return a new {@code Option} object
     */
    private static Option load(final String name, final boolean def, final String argFalse, final String argTrue) {
        return new Option(name, def, argFalse, argTrue);
    }

    /**
     * Returns the boolean value of the specified option.
     * If the option was not provided in the arguments, the default value is used.
     *
     * @param name the name of the option
     * @return the boolean value of the specified option, or {@code false} if not found
     */
    public boolean getOption(final String name) {
        return loadedOptions.getOrDefault(name, false);
    }

    /**
     * A static class representing a single option that can be configured via command-line arguments.
     */
    private static class Option {
        /**
         * The name of the option.
         */
        public final String name;

        /**
         * The default boolean value of the option.
         */
        public final boolean def;

        /**
         * The argument that represents the "false" value of the option.
         */
        public final String argFalse;

        /**
         * The argument that represents the "true" value of the option.
         */
        public final String argTrue;

        /**
         * Constructs a new {@code Option} object with the given parameters.
         *
         * @param name     the name of the option
         * @param def      the default boolean value for the option
         * @param argFalse the argument that corresponds to the "false" value of the option
         * @param argTrue  the argument that corresponds to the "true" value of the option
         */
        private Option(final String name, final boolean def, final String argFalse, final String argTrue) {
            this.name = name;
            this.def = def;
            this.argFalse = "-" + argFalse;
            this.argTrue = "-" + argTrue;
        }
    }
}