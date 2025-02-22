package me.kuwg.clarity.info;

import me.kuwg.clarity.Clarity;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code ClarityRunningInformation} class is responsible for managing and loading
 * configuration options from the command-line arguments. It interprets specific arguments
 * and determines their boolean values based on predefined options.
 */
@SuppressWarnings("SpellCheckingInspection")
public final class ClarityRunningInformation {

    /**
     * Array of options available for configuration.
     * Each option consists of a name, a default boolean value, and its corresponding
     * true and false argument representations.
     */
    private static final Option[] OPTIONS = {
            // optimization (deprecated)
            load("optimize", true, "noopt", "opt", "Deprecated option for AST optimization."),

            // verbose (log)
            load("verbose", false, "noverb", "verb", "Enables startup verbose."),

            // starting info (jvm startup times)
            load("startinfo", false, "nsinfo", "sinfo", "Enables debugging JVM startup timings information."),

            // speed info (graph)
            load("speedinfo", false, "nspeedinfo", "speedinfo", "Creates a graph at the end of the interpretation that describes node speed."),

            // default natives (include)
            load("defaults", true, "nodef", "def", "Enables including default natives."),

            // load default natives in interpreter
            load("loadnatives", false, "nlnat", "loadnatives", "Enables pre-loading for known default natives in the interpreter."),

            // compiler version
            load("compilerversion", false, "nocompver", "compver", "Enables debugging for compiler version.")
    };

    /**
     * A map that holds the loaded options from the command-line arguments.
     * The key is the option name, and the value is its boolean status.
     */
    private final Map<String, Boolean> loadedOptions;

    /**
     * Constructor that initializes the {@code ClarityRunningInformation} object
     * by parsing command-line arguments and loading the corresponding options.
     */
    public ClarityRunningInformation() {
        loadedOptions = new HashMap<>();

        final String[] args = Clarity.ARGS;

        // Parse the arguments and load options based on true/false values
        int count = 2;
        for (int i = 2; i < args.length; i++) {
            final String arg = args[i];
            for (final Option option : OPTIONS) {
                if (arg.equals(option.argTrue)) {
                    loadedOptions.put(option.name, true);
                    count++;
                } else if (arg.equals(option.argFalse)) {
                    loadedOptions.put(option.name, false);
                    count++;
                }
            }
        }
        Clarity.ASC = count;
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
    private static Option load(final String name, final boolean def, final String argFalse, final String argTrue, final String description) {
        return new Option(name, def, argFalse, argTrue, description);
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
     * Prints all the options and their description.
     */
    public static void printOptions() {
        final StringBuilder sb = new StringBuilder("Printing options:\n");

        for (final Option option : OPTIONS) {
            sb.append("\t").append(option.name).append(": ")
                    .append("\n\t\tDefault: ").append(option.def)
                    .append("\n\t\tEnable: '").append(option.argTrue).append("'")
                    .append("\n\t\tDisable: '").append(option.argFalse).append("'")
                    .append("\n\t\tDescription: ").append(option.description)
                    .append("\n");
        }

        System.out.println(sb);
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
         * The description of the option.
         */
        public final String description;

        /**
         * Constructs a new {@code Option} object with the given parameters.
         *
         * @param name        the name of the option
         * @param def         the default boolean value for the option
         * @param argFalse    the argument that corresponds to the "false" value of the option
         * @param argTrue     the argument that corresponds to the "true" value of the option
         * @param description the description of the option
         */
        private Option(final String name, final boolean def, final String argFalse, final String argTrue, final String description) {
            this.name = name;
            this.def = def;
            this.argFalse = "-" + argFalse;
            this.argTrue = "-" + argTrue;
            this.description = description;
        }
    }
}