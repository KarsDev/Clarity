package me.kuwg.clarity.nmh;

import me.kuwg.clarity.library.ClarityNativeLibrary;
import me.kuwg.clarity.library.cnc.ClarityNativeClass;
import me.kuwg.clarity.library.cnf.ClarityNativeFunction;
import me.kuwg.clarity.nmh.natives.impl.clazz.FileNativeClass;
import me.kuwg.clarity.nmh.natives.impl.clazz.MathNativeClass;
import me.kuwg.clarity.nmh.natives.impl.clazz.ReflectionsNativeClass;
import me.kuwg.clarity.nmh.natives.impl.def.*;

public class LocalClarityNativeLibrary implements ClarityNativeLibrary {

    protected LocalClarityNativeLibrary() {
    }

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

        };
    }

    @Override
    public ClarityNativeClass[] getLibraryNativeClasses() {
        return new ClarityNativeClass[] {
                new MathNativeClass(),
                new ReflectionsNativeClass(),
                new FileNativeClass(),

        };
    }
}
