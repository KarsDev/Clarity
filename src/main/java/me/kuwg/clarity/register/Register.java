package me.kuwg.clarity.register;

/**
 * A utility class that handles function and method call registration, error reporting,
 * and exception handling within the Clarity environment.
 * <p>
 * This class maintains a {@link RegisterStack} to keep track of function calls and other
 * operations, and provides methods for logging errors, throwing exceptions, and printing
 * the current state of the register stack.
 * </p>
 */
public final class Register {

    /**
     * The register stack that holds elements representing various operations.
     */
    private static final RegisterStack stack = new RegisterStack(20);

    /**
     * Registers a new {@link RegisterElement} into the register stack.
     *
     * @param element The element to be registered.
     */
    public static void register(final RegisterElement element) {
        stack.push(element);
    }

    /**
     * Retrieves the current register stack.
     *
     * @return The current {@link RegisterStack}.
     */
    public static RegisterStack getStack() {
        return stack;
    }

    /**
     * Throws an exception with a message, line information, and the current state of the register.
     * <p>
     * This method logs an error, prints the register stack, exits the system, and raises
     * a {@link RegisterException}.
     * </p>
     *
     * @param message The error message to display.
     * @param line    The line number where the error occurred.
     */
    public static void throwException(final String message, final String line) {
        error(message, line);
        printRegister();
        exit();
        raise();
    }

    /**
     * Throws an exception with a message and the current state of the register.
     * <p>
     * This method logs an error, prints the register stack, exits the system, and raises
     * a {@link RegisterException}.
     * </p>
     *
     * @param message The error message to display.
     */
    public static void throwException(final String message) {
        error(message);
        printRegister();
        exit();
        raise();
    }

    /**
     * Throws an exception with a message and line number, and the current state of the register.
     * <p>
     * This method converts the line number to a string and calls the
     * {@link #throwException(String, String)} method.
     * </p>
     *
     * @param message The error message to display.
     * @param line    The line number where the error occurred.
     */
    public static void throwException(final String message, final int line) {
        throwException(message, String.valueOf(line));
    }

    /**
     * Logs an error message to the standard error stream.
     *
     * @param message The error message to display.
     */
    private static void error(final String message) {
        System.err.println("An error occurred: " + message);
    }

    /**
     * Logs an error message with line information to the standard error stream.
     *
     * @param message The error message to display.
     * @param line    The line number where the error occurred.
     */
    private static void error(final String message, final String line) {
        System.err.println("An error occurred: " + message + "\nAt line " + line);
    }

    /**
     * Prints the current state of the register stack to the standard error stream.
     * <p>
     * If the stack is empty, a message indicating that the register is empty will be printed.
     * Otherwise, each element in the stack is printed in reverse order.
     * </p>
     */
    private static void printRegister() {
        if (!stack.isEmpty()) {
            System.err.println("Register:");
            for (int i = stack.size() - 1; i >= 0; i--) System.err.println("    " + stack.pop());
        } else System.err.println("Register is empty.");
    }

    /**
     * Exits the application with a status code of -1.
     */
    private static void exit() {
        System.exit(-1);
    }

    /**
     * Raises a {@link RegisterException} to indicate an error in the register.
     */
    static void raise() {
        throw new RegisterException();
    }

    /**
     * An enumeration of the different types of register elements that can be stored
     * in the {@link RegisterStack}.
     */
    public enum RegisterElementType {
        FUNCALL,     // Function call
        NATIVECALL,  // Native call
        CLASSINST,   // Class instantiation
        STATICCALL,  // Static call
        ARRAYCALL,   // Array function call
        LOCALCALL,   // Local function call
        STRINGCALL,  // String function call
        STATICINIT   // Static initialization
    }

    /**
     * Represents an element in the register stack.
     * <p>
     * Each element has a type, a parameter (such as a method or function name), a line number,
     * and the name of the class in which the operation occurred.
     * </p>
     */
    public static class RegisterElement {
        private final RegisterElementType type;
        private final String param;
        private final int line;
        private final String currentClass;

        /**
         * Constructs a new {@link RegisterElement} with the specified type, parameter, line number,
         * and class name.
         *
         * @param type         The type of the register element.
         * @param param        The parameter or name of the operation (e.g., method name).
         * @param line         The line number where the operation occurred.
         * @param currentClass The name of the class in which the operation occurred.
         */
        public RegisterElement(final RegisterElementType type, final String param, final int line, final String currentClass) {
            this.type = type;
            this.param = param;
            this.line = line;
            this.currentClass = currentClass;
        }

        /**
         * Returns the type of this register element.
         *
         * @return The type of the element.
         */
        public final RegisterElementType getType() {
            return type;
        }

        /**
         * Returns the parameter of this register element.
         *
         * @return The parameter or name of the operation (e.g., method name).
         */
        public final String getParam() {
            return param;
        }

        /**
         * Returns the line number where this register element was created.
         *
         * @return The line number.
         */
        public final int getLine() {
            return line;
        }

        /**
         * Returns the name of the class in which this register element was created.
         *
         * @return The class name.
         */
        public final String getCurrentClass() {
            return currentClass;
        }

        /**
         * Returns a string representation of this register element, including the type of operation,
         * the parameter, the class name, and the line number.
         *
         * @return A formatted string representing this register element.
         */
        @Override
        public String toString() {
            switch (getType()) {
                case FUNCALL: {
                    return "function call " + getParam() + formatClass() + formatLine();
                }
                case ARRAYCALL: {
                    return "array function " + getParam() + formatClass() + formatLine();
                }
                case NATIVECALL: {
                    return "native call " + getParam() + formatClass() + formatLine();
                }
                case CLASSINST: {
                    return "init class " + getParam() + formatClass() + formatLine();
                }
                case LOCALCALL: {
                    return "local function call " + getParam() + formatClass() + formatLine();
                }
                case STATICCALL: {
                    return "static call " + getParam() + formatClass() + formatLine();
                }
                case STRINGCALL: {
                    return "string function " + getParam() + formatClass() + formatLine();
                }
                default: {
                    throwException("Unsupported register element type: " + getType());
                    return null;
                }
            }
        }

        /**
         * Formats the class information for this register element.
         *
         * @return A formatted string representing the class information.
         */
        private String formatClass() {
            return "none".equals(getCurrentClass()) ? " in no class" : " in class " + getCurrentClass();
        }

        /**
         * Formats the line information for this register element.
         *
         * @return A formatted string representing the line number.
         */
        private String formatLine() {
            return getLine() == 0 ? " (unknown line)" : ", at line: " + getLine();
        }
    }
}