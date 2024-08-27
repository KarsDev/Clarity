package me.kuwg.clarity.privilege;

import me.kuwg.clarity.interpreter.register.Register;

public class Privileges {

    private static final String[] PRIVILEGED_CLASS_NAMES = new String[] {
            "System",
    };

    public static void checkClassName(final String name, final int line) {
        for (final String privilegedClassName : PRIVILEGED_CLASS_NAMES) {
            if (privilegedClassName.equals(name)) {
                Register.throwException("Invalid class name (privileged): " + name, line);
            }
        }
    }
}
