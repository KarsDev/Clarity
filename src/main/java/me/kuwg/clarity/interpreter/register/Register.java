package me.kuwg.clarity.interpreter.register;

public final class Register {

    private static final RegisterStack stack = new RegisterStack(20);


    public static void addElement(final String element, final int line) {
        stack.push(element + " at line: " + line);
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
        error(message, line);
        printRegister();
        exit();
    }

    public static void throwException(final int line) {
        error("?", line);
        printRegister();
        exit();
    }

    public static void throwException(final String message) {
        error(message, "?");
        printRegister();
        exit();
    }

    public static void throwException() {
        error("?", "?");
        printRegister();
        exit();
    }

    private static void error(final String message, final Object line) {
        System.err.println("An error occurred: '" + message + "' at line " + line);
    }

    private static void printRegister() {
        System.err.println("Register:");
        for (int i = 0; i < stack.size(); i++) System.err.println("    " + stack.pop());
    }

    private static void exit() {
        System.exit(-1);
    }
}
