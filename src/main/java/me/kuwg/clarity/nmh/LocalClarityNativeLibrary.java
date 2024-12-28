package me.kuwg.clarity.nmh;

import me.kuwg.clarity.library.ClarityNativeLibrary;
import me.kuwg.clarity.library.natives.ClarityNativeClass;
import me.kuwg.clarity.library.natives.ClarityNativeFunction;
import me.kuwg.clarity.nmh.natives.impl.clazz.FileNativeClass;
import me.kuwg.clarity.nmh.natives.impl.clazz.MathNativeClass;
import me.kuwg.clarity.nmh.natives.impl.clazz.ReflectionsNativeClass;
import me.kuwg.clarity.nmh.natives.impl.def.*;

/**
 * The {@code LocalClarityNativeLibrary} class provides a set of native functions and classes
 * for the Clarity interpreter. This class serves as a local implementation of the
 * {@link ClarityNativeLibrary} interface, encapsulating various utility functions and
 * classes that can be utilized within the interpreter.
 */
public final class LocalClarityNativeLibrary implements ClarityNativeLibrary {

    /**
     * Constructs a new instance of {@code LocalClarityNativeLibrary}.
     * The constructor is protected to restrict instantiation from outside the package.
     */
    LocalClarityNativeLibrary() {
    }

    /**
     * Returns an array of native functions provided by this library.
     *
     * @return an array of {@link ClarityNativeFunction} instances that are available in this library
     */
    @Override
    public ClarityNativeFunction<?>[] getLibraryNativeFunctions() {
        return new ClarityNativeFunction[] {
                new PrintlnNative(),
                new InputNative(),
                new PrintNative(),
                new NowNative(),
                new NanoNative(),
                new ErrNative(),
                new ExecNative(),
                new SleepNative(),
                new TypeOfNative(),
                new SortNative(),
                new ArrayNative(),
                new EvalNative(),

        };
    }

    /**
     * Returns an array of native classes provided by this library.
     *
     * @return an array of {@link ClarityNativeClass} instances that are available in this library
     */
    @Override
    public ClarityNativeClass[] getLibraryNativeClasses() {
        return new ClarityNativeClass[] {
                new MathNativeClass(),
                new ReflectionsNativeClass(),
                new FileNativeClass(),
        };
    }
}