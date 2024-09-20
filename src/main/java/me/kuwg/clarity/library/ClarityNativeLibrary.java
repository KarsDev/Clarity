package me.kuwg.clarity.library;

import me.kuwg.clarity.library.cnc.ClarityNativeClass;
import me.kuwg.clarity.library.cnf.ClarityNativeFunction;

/**
 * An interface representing a library of native functions and classes in the Clarity language.
 * Implementations of this interface should provide a collection of natives
 * that can be used within the Clarity environment.
 */
public interface ClarityNativeLibrary {

    /**
     * Retrieves an array of native functions provided by this library.
     *
     * @return An array of {@link ClarityNativeFunction} objects representing the native functions
     *         available in this library.
     */
    ClarityNativeFunction<?>[] getLibraryNativeFunctions();

    /**
     * Retrieves an array of native classes provided by this library.
     *
     * @return An array of {@link ClarityNativeClass} objects representing the native classes
     *         available in this library.
     */
    ClarityNativeClass[] getLibraryNativeClasses();
}
