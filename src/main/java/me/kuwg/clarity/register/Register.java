package me.kuwg.clarity.register;

public final class Register {

    private static final RegisterStack stack = new RegisterStack(20);

    public static void register(final RegisterElement element) {
        stack.push(element);
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

    public enum RegisterElementType {
        FUNCALL, NATIVECALL, CLASSINST, STATICCALL, ARRAYCALL, LOCALCALL
    }

    public static class RegisterElement {
        private final RegisterElementType type;
        private final String param;
        private final int line;
        private final String currentClass;

        public RegisterElement(final RegisterElementType type, final String param, final int line, final String currentClass) {
            this.type = type;
            this.param = param;
            this.line = line;
            this.currentClass = currentClass;
        }

        public final RegisterElementType getType() {
            return type;
        }

        public final String getParam() {
            return param;
        }

        public final int getLine() {
            return line;
        }

        public final String getCurrentClass() {
            return currentClass;
        }

        @Override
        public String toString() {
            switch (type) {
                case FUNCALL: {
                    return "function call " + param + formatClass() + formatLine();
                }
                case ARRAYCALL: {
                    return "array function " + param + formatClass() + formatLine();
                }
                case NATIVECALL: {
                    return "native call " + param + formatClass() + formatLine();
                }
                case CLASSINST: {
                    return "init class " + param + formatClass() + formatLine();
                }
                case LOCALCALL: {
                    return "local function call " + param + formatClass() + formatLine();
                }
                case STATICCALL: {
                    return "static call " + param + formatClass() + formatLine();
                }
                default: {
                    throwException("Unsupported register element type: " + type);
                    return null;
                }
            }
        }

        private String formatClass() {
            return "none".equals(currentClass) ? " in no class" : " in class " + currentClass;
        }

        private String formatLine() {
            return line == 0 ? " (unknown line)" : ", at line: " + line;
        }
    }
}

