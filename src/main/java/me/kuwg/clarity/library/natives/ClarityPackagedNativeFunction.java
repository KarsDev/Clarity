package me.kuwg.clarity.library.natives;

import java.util.List;

/**
 * An abstract class that represents a packaged native function in the Clarity language.
 * This class extends {@link ClarityNativeFunction} and is intended for native functions
 * that belong to a specific package.
 *
 * @param <R> The return type of the native function.
 */
public abstract class ClarityPackagedNativeFunction<R> extends ClarityNativeFunction<R> {

    /**
     * The package name associated with this native function.
     */
    private final String pkg;

    /**
     * Constructs a new {@code ClarityPackagedNativeFunction} with the specified function name and package.
     *
     * @param name The name of the native function.
     * @param pkg The custom package of the native function.
     */
    protected ClarityPackagedNativeFunction(final String name, final String pkg) {
        super(name);
        this.pkg = pkg;
    }

    /**
     * Constructs a new {@code ClarityPackagedNativeFunction} with the specified function name.
     * The package is derived from the class's package name, starting at the 38th character.
     *
     * @param name The name of the native function.
     */
    protected ClarityPackagedNativeFunction(final String name) {
        super(name);
        this.pkg = getClass().getPackage().getName().substring(37);
    }

    /**
     * Checks if the function can be called by the class with the specified name.
     *
     * @param className The name of the class attempting to call the function.
     * @return {@code true} if the function can be called by the specified class;
     *         {@code false} otherwise.
     */
    protected abstract boolean canCall(final String className);

    /**
     * Determines if this function applies based on the specified package, name,
     * caller class, and parameters.
     *
     * @param pkg The package to match.
     * @param name The function name to match.
     * @param callerClass The name of the class calling the function.
     * @param params The parameters to be passed to the function.
     * @return {@code true} if the package, function name, and class name match,
     *         and the parameters are valid for this function; {@code false} otherwise.
     */
    public final boolean applies(final String pkg, final String name, final String callerClass, final List<Object> params) {
        return pkg.equals(this.pkg) && applies(name, params) && canCall(callerClass);
    }

    /**
     * Checks if the function can be applied based on its name and the given parameters.
     * This method overrides the {@link ClarityNativeFunction#applies(String, List)} method.
     *
     * @param name The name of the function to check.
     * @param params The parameters to validate.
     * @return {@code true} if the function's name matches, and it can be applied
     *         with the given parameters; {@code false} otherwise.
     */
    @Override
    public boolean applies(final String name, final List<Object> params) {
        return super.applies(name, params);
    }

    /**
     * Returns the fully qualified name of this function, including its package name.
     *
     * @return The fully qualified name of the native function.
     */
    public final String getFullyQualifiedName() {
        return this.pkg + "." + getName();
    }
}