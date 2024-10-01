package me.kuwg.clarity.library.natives;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.register.Register;

import java.util.List;

/**
 * Abstract base class for native classes within the Clarity library.
 * This class defines common behavior for handling method calls, type checking, and error handling.
 */
public abstract class ClarityNativeClass {
    /** Singleton instance representing a void object. */
    protected static final VoidObject VOID = VoidObject.VOID_OBJECT;

    /** The name of the native class. */
    private final String name;

    /**
     * Constructs a new instance of {@code ClarityNativeClass} with the specified name.
     *
     * @param name The name of the class.
     */
    protected ClarityNativeClass(final String name) {
        this.name = name;
    }

    /**
     * Retrieves the name of this native class.
     *
     * @return The name of the class.
     */
    public final String getName() {
        return name;
    }

    /**
     * Handles a method call on this native class with the specified method name, parameters, and context.
     *
     * @param name The name of the method being invoked.
     * @param params The list of parameters passed to the method.
     * @param context The execution context for the method call.
     * @return The result of the method call.
     * @throws Exception If an error occurs during method execution.
     */
    public abstract Object handleCall(final String name, final List<Object> params, final Context context) throws Exception;

    /**
     * Checks a set of conditions and throws an exception with the specified error message if any condition is false.
     *
     * @param err The error message to use if a condition fails.
     * @param condos A set of boolean conditions to be checked.
     */
    protected final void check(final String err, final boolean... condos) {
        for (final boolean condo : condos) {
            if (!condo) {
                Register.throwException(err);
            }
        }
    }

    /**
     * Retrieves a string representation of the parameter types from the given list of parameters.
     * This is used for type-checking or debugging purposes.
     *
     * @param params A list of parameters whose types need to be identified.
     * @return A string representing the types of the parameters.
     */
    protected final String getParamTypes(List<Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (sb.length() > 0) sb.append(", ");
            if (param == null) sb.append("null");
            else if (param instanceof Integer) sb.append("int");
            else if (param instanceof Double) sb.append("float");
            else if (param instanceof String) sb.append("str");
            else if (param instanceof Object[]) sb.append("arr");
            else sb.append(param.getClass().getSimpleName());
        }
        return sb.toString();
    }
}