package me.kuwg.clarity.nmh;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.library.ClarityNativeLibrary;
import me.kuwg.clarity.library.natives.ClarityNativeClass;
import me.kuwg.clarity.library.natives.ClarityNativeFunction;
import me.kuwg.clarity.library.natives.ClarityPackagedNativeFunction;
import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;
import me.kuwg.clarity.nmh.natives.abstracts.PackagedNativeFunction;
import me.kuwg.clarity.nmh.natives.impl.pkg.date.NowDateNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.error.ThrowNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.*;
import me.kuwg.clarity.nmh.natives.impl.pkg.util.CreateListNative;
import me.kuwg.clarity.register.Register;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code NativeMethodHandler} class is responsible for managing the registration, invocation,
 * and handling of native methods within the Clarity interpreter. This includes support for both
 * default native functions and packaged native functions, as well as class-level native method calls.
 * <p>
 * Native functions can be thought of as extensions to the interpreter's capabilities, allowing for
 * direct calls to system-level or utility functions that are not defined within the standard
 * interpreter language.
 * </p>
 *
 * <p>
 * For more information on native methods and how they integrate with the Clarity interpreter,
 * refer to the official documentation or the {@link me.kuwg.clarity.library.ClarityNativeLibrary}
 * class.
 * </p>
 */
public final class NativeMethodHandler {

    private static final Map<String, ClarityNativeFunction<?>> defaultFunctions = new HashMap<>();
    private static final Map<String, ClarityPackagedNativeFunction<?>> packagedFunctions = new HashMap<>();
    private static final Map<String, ClarityNativeClass> nativeClasses = new HashMap<>();

    /**
     * Constructs a {@code NativeMethodHandler} instance, initializing the handler by
     * registering default libraries and packaged native functions necessary for interpreter
     * operations.
     */
    public NativeMethodHandler() {
        initializeDefaultLibrary();
        initializePackagedFunctions();
    }

    /**
     * Registers default native functions from the local native library into the handler.
     * This step is crucial for ensuring that the interpreter has access to core functionalities
     * provided by the native library.
     */
    private void initializeDefaultLibrary() {
        loadLibrary(new LocalClarityNativeLibrary());
    }

    /**
     * Registers all available packaged native functions into the handler.
     * <p>
     * Packaged functions may require different accessors depending on their functionalities,
     * which are categorized during registration. The current implementation includes functions
     * for system management and date handling.
     * </p>
     */
    private void initializePackagedFunctions() {
        // required accessor: none
        registerPackagedFunction(new CreateListNative());
        registerPackagedFunction(new ThrowNative());

        // required accessor: System
        registerPackagedFunction(new ExitNative());
        registerPackagedFunction(new CheckNativeTypeNative());
        registerPackagedFunction(new LoadNativeLibraryNative());
        registerPackagedFunction(new CurrentThreadNameNative());
        registerPackagedFunction(new LoadJarNativeLibraryNative());
        registerPackagedFunction(new IdentityHashCodeNative());

        // required accessor: Date
        registerPackagedFunction(new NowDateNative());
    }

    /**
     * Registers a specified packaged native function, adding it to the internal map of functions
     * for easy retrieval during invocation.
     *
     * @param function the {@link PackagedNativeFunction} to register
     */
    private void registerPackagedFunction(final PackagedNativeFunction<?> function) {
        packagedFunctions.put(function.getFullyQualifiedName(), function);
    }

    /**
     * Invokes a default native function by its name, passing in the provided parameters.
     *
     * @param name   the name of the native function to invoke
     * @param params the list of parameters to provide to the function
     * @return the result of the function call, or {@code null} if an error occurs
     * @throws IllegalArgumentException if the function is not found or not accessible
     */
    public Object callDefault(final String name, final List<Object> params) {
        final ClarityNativeFunction<?> method = defaultFunctions.get(name);

        if (method != null && method.applies(name, params)) {
            return method.call(params);
        }

        Register.throwException("Default native function " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return VoidObject.VOID_OBJECT;
    }

    /**
     * Invokes a packaged native function based on the provided package and function name,
     * along with the calling class and parameters.
     *
     * @param pkg         the package name of the function
     * @param name        the name of the function to invoke
     * @param callerClass the class invoking the function
     * @param params      the list of parameters to provide to the function
     * @return the result of the function call, or {@code null} if an error occurs
     * @throws IllegalArgumentException if the function is not found or not accessible
     */
    public Object callPackaged(final String pkg, final String name, final String callerClass, final List<Object> params) {
        final String key = pkg + "." + name;
        final ClarityPackagedNativeFunction<?> function = packagedFunctions.get(key);

        if (function != null && function.applies(pkg, name, callerClass, params)) {
            return function.call(params);
        }

        Register.throwException("Packaged native function " + pkg + (pkg.endsWith(".") ? "" : ".") + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return VoidObject.VOID_OBJECT;
    }

    /**
     * Invokes a native method from a registered native class, providing the necessary parameters
     * and context for execution.
     *
     * @param name    the name of the native class containing the method
     * @param method  the name of the method to invoke within the class
     * @param params  the parameters to pass to the method
     * @param context the current execution context, providing necessary environmental information
     * @return the result of the class method call, or {@code null} if an error occurs
     * @throws IllegalArgumentException if the class or method is not found or not accessible
     */
    public Object callClassNative(final String name, final String method, final List<Object> params, final Context context) {
        final ClarityNativeClass clazz = nativeClasses.get(name);
        if (clazz != null) {
            try {
                return clazz.handleCall(method, params, context);
            } catch (final Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
                return null;
            }
        }

        Register.throwException("Class native function " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return VoidObject.VOID_OBJECT;
    }

    public boolean help(final String name) {
        final ClarityNativeFunction<?> cnf = defaultFunctions.get(name);

        if (!(cnf instanceof DefaultNativeFunction<?>)) return false;

        System.out.println("Printing help for default native function " + name);

        ((DefaultNativeFunction<?>) cnf).help();

        return true;
    }

    /**
     * Converts a list of objects into a comma-separated string of their respective class names.
     *
     * @param objects the list of objects to convert
     * @return a string representation of the objects' class names, in lowercase
     */
    private String objectsToClassesString(final List<Object> objects) {
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            if (i > 0) s.append(", ");
            s.append(objects.get(i).getClass().getSimpleName().toLowerCase());
        }
        return s.toString();
    }

    /**
     * Loads a specified {@link ClarityNativeLibrary} and registers its native functions
     * and classes within the handler for future invocation.
     *
     * @param lib the native library to load
     */
    public static void loadLibrary(final ClarityNativeLibrary lib) {
        for (final ClarityNativeFunction<?> function : lib.getLibraryNativeFunctions()) {
            defaultFunctions.put(function.getName(), function);
        }

        for (final ClarityNativeClass clazz : lib.getLibraryNativeClasses()) {
            nativeClasses.put(clazz.getName(), clazz);
        }

        for (final ClarityPackagedNativeFunction<?> function : lib.getPackagedNativeFunctions()) {
            packagedFunctions.put(function.getFullyQualifiedName(), function);
        }
    }
}