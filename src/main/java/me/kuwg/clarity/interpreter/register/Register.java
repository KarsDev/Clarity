package me.kuwg.clarity.interpreter.register;

public final class Register {

    private static final RegisterStack stack = new RegisterStack(20);

    public static void addElement(final String element, final int line, final String currentClass) {
        stack.push(element + " at line: " + line + (currentClass != null ? " in class " + currentClass : ""));
    }

    public static void removeElement() {
        if (!stack.isEmpty()) stack.pop();
    }

    public static RegisterStack getStack() {
        return stack;
    }

    public static void throwException(final String message, final String line) {
        error(message, line);
        printRegister();
        exit();
    }

    public static void throwException(final String message, final int line) {
        throwException(message, String.valueOf(line));
    }

    public static void throwException(final String message) {
        error(message);
        printRegister();
        exit();
    }

    public static void throwException() {
        error("?");
        printRegister();
        exit();
    }

    private static void error(final String message) {
        System.err.println("An error occurred: " + message);
    }

    private static void error(final String message, final String line) {
        System.err.println("An error occurred: " + message + "\nAt line " + line);
    }

    private static void printRegister() {
        System.err.println("Register:");
        for (int i = stack.size() - 1; i >= 0; i--) System.err.println("    " + stack.pop());
    }

    private static void exit() {
        System.exit(-1);
    }
}
