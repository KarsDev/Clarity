package me.kuwg.clarity.library.cnf;

import me.kuwg.clarity.library.objects.VoidObject;

import java.util.List;

/**
 * An abstract class representing a native function in the Clarity language.
 * This class defines the structure for implementing native functions
 * with a specific return type.
 *
 * @param <R> The return type of the native function.
 */
public abstract class ClarityNativeFunction<R> {

    /**
     * A constant representing the void object used in functions that do not return a value.
     */
    protected static final VoidObject VOID = VoidObject.VOID_OBJECT;

    /**
     * The name of the native function.
     */
    private final String name;

    /**
     * Constructs a new ClarityNativeFunction with the given name.
     *
     * @param name The name of the native function.
     */
    protected ClarityNativeFunction(final String name) {
        this.name = name;
    }

    /**
     * Abstract method that defines the behavior of the native function when called with the given parameters.
     *
     * @param params The parameters to pass to the function.
     * @return The result of the function call.
     */
    public abstract R call(final List<Object> params);

    /**
     * Abstract method that checks if the function can be applied with the given parameters.
     * This method is meant to be overridden by subclasses to provide specific validation logic.
     *
     * @param params The parameters to validate.
     * @return {@code true} if the function can be applied with the given parameters; {@code false} otherwise.
     */
    protected abstract boolean applies0(final List<Object> params);

    /**
     * Checks if this function can be applied based on its name and the given parameters.
     *
     * @param name The name to check against the function's name.
     * @param params The parameters to validate.
     * @return {@code true} if the function's name matches and it can be applied with the given parameters;
     *         {@code false} otherwise.
     */
    public boolean applies(final String name, final List<Object> params) {
        return this.name.equals(name) && applies0(params);
    }

    /**
     * Returns the name of the native function.
     *
     * @return The name of the native function.
     */
    public final String getName() {
        return name;
    }
}