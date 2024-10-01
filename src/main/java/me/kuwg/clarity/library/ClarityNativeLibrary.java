package me.kuwg.clarity.library;

import me.kuwg.clarity.library.natives.ClarityNativeClass;
import me.kuwg.clarity.library.natives.ClarityNativeFunction;
import me.kuwg.clarity.library.natives.ClarityPackagedNativeFunction;

/**
 * An interface representing a library of native functions and classes in the Clarity language.
 * Implementations of this interface should provide a collection of native functions and classes
 * that can be used within the Clarity environment.
 * <p>
 * Native libraries allow for extending the capabilities of the Clarity interpreter by integrating
 * functions and classes that are implemented outside the core language. This enables users to
 * access system-level functionalities, utility methods, or custom operations defined in
 * native code.
 * </p>
 */
public interface ClarityNativeLibrary {

    /**
     * Retrieves an array of native functions provided by this library.
     * <p>
     * Each {@link ClarityNativeFunction} in the returned array represents a function that can
     * be called from the Clarity interpreter. These functions may perform various tasks,
     * including mathematical operations, string manipulations, or system interactions.
     * </p>
     * <p>
     * Implementations of this method should return an array containing all available native
     * functions. If no functions are available, an empty array will be returned to indicate
     * the absence of native functions without causing an exception.
     * </p>
     *
     * @return An array of {@link ClarityNativeFunction} objects representing the native functions
     *         available in this library. If no functions are available, an empty array will be returned.
     */
    default ClarityNativeFunction<?>[] getLibraryNativeFunctions() {
        return new ClarityNativeFunction<?>[0]; // Return an empty array by default
    }

    /**
     * Retrieves an array of native classes provided by this library.
     * <p>
     * Each {@link ClarityNativeClass} in the returned array represents a class that can
     * be instantiated or used within the Clarity interpreter. These classes may encapsulate
     * specific functionalities or group related methods together for organizational purposes.
     * </p>
     * <p>
     * Implementations of this method should return an array containing all available native
     * classes. If no classes are available, an empty array will be returned to indicate
     * the absence of native classes without causing an exception.
     * </p>
     *
     * @return An array of {@link ClarityNativeClass} objects representing the native classes
     *         available in this library. If no classes are available, an empty array will be returned.
     */
    default ClarityNativeClass[] getLibraryNativeClasses() {
        return new ClarityNativeClass[0]; // Return an empty array by default
    }

    /**
     * Retrieves an array of packaged native functions provided by this library.
     * <p>
     * Each {@link ClarityPackagedNativeFunction} in the returned array represents a function that
     * belongs to a specific package and can be called from the Clarity interpreter. These functions
     * are grouped by their package for better organization and are often used in specialized contexts.
     * </p>
     * <p>
     * Implementations of this method should return an array containing all available packaged native
     * functions. If no packaged functions are available, an empty array will be returned to indicate
     * the absence of packaged native functions without causing an exception.
     * </p>
     *
     * @return An array of {@link ClarityPackagedNativeFunction} objects representing the packaged
     *         native functions available in this library. If no functions are available, an empty array will be returned.
     */
    default ClarityPackagedNativeFunction<?>[] getPackagedNativeFunctions() {
        return new ClarityPackagedNativeFunction<?>[0]; // Return an empty array by default
    }
}