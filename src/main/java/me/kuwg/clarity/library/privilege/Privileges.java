package me.kuwg.clarity.library.privilege;

import me.kuwg.clarity.register.Register;

import java.util.Arrays;
import java.util.List;

/**
 * The {@code Privileges} class manages a list of class names that are considered privileged.
 * It provides functionality to validate class names and ensure that restricted class names
 * cannot be used in contexts where they are not allowed. If a class name is deemed privileged,
 * an exception is thrown.
 */
public class Privileges {

    /**
     * A list of privileged class names that are restricted and cannot be used.
     * These class names are reserved for system-level operations and should not be
     * instantiated or used directly in user-defined code.
     */
    public static final List<String> PRIVILEGED_CLASS_NAMES = Arrays.asList(
            "System",
            "Math",
            "Reflections",
            "Class"
    );

    /**
     * Checks whether the provided class name is in the list of privileged class names.
     * If the class name is found to be privileged, an exception is thrown with a detailed
     * error message and the line number where the violation occurred.
     *
     * <p>This method uses Java Streams to filter the list of privileged class names
     * and throws an exception if the provided class name is found.</p>
     *
     * @param name The name of the class to check.
     * @param line The line number where the class name appears, used for error reporting.
     * @throws RuntimeException if the class name is privileged.
     */
    public static void checkClassName(final String name, final int line) {
        // Check if the provided class name is in the list of privileged class names
        PRIVILEGED_CLASS_NAMES.stream()
                .filter(privilegedClassName -> privilegedClassName.equals(name))
                .findFirst()
                .ifPresent(str -> Register.throwException(
                        "Invalid class name (privileged): " + name, line));
    }
}