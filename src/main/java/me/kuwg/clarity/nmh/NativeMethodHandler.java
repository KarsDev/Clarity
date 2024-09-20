package me.kuwg.clarity.nmh;

import me.kuwg.clarity.library.ClarityNativeClass;
import me.kuwg.clarity.library.ClarityNativeFunction;
import me.kuwg.clarity.library.ClarityNativeLibrary;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.impl.pkg.date.GetDayNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.date.GetMonthNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.date.GetWeekDayNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.date.GetYearNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.CheckNativeTypeNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.CurrentThreadNameNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.LoadNativeLibraryNative;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;
import me.kuwg.clarity.nmh.natives.aclass.NativeClass;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeFunction;
import me.kuwg.clarity.nmh.natives.impl.clazz.*;
import me.kuwg.clarity.nmh.natives.impl.def.*;
import me.kuwg.clarity.nmh.natives.impl.pkg.error.ExceptNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.ExitNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.util.CreateListNative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeMethodHandler {

    private static final Map<String, ClarityNativeFunction<?>> defaultFunctions = new HashMap<>();
    private static final Map<String, PackagedNativeFunction<?>> packagedFunctions = new HashMap<>();
    private static final Map<String, ClarityNativeClass> nativeClasses = new HashMap<>();

    public NativeMethodHandler() {
        initializeDefaultFunctions();
        initializePackagedFunctions();
        initializeNativeClasses();
    }

    private void initializeDefaultFunctions() {
        registerDefaultFunction(new PrintlnNative());
        registerDefaultFunction(new InputNative());
        registerDefaultFunction(new PrintNative());
        registerDefaultFunction(new NowNative());
        registerDefaultFunction(new NanoNative());
        registerDefaultFunction(new ErrNative());
        registerDefaultFunction(new ExecNative());
        registerDefaultFunction(new SleepNative());
        registerDefaultFunction(new TypeOfNative());
    }

    private void initializePackagedFunctions() {
        // can be accessed by anyone
        registerPackagedFunction(new CreateListNative());
        registerPackagedFunction(new ExceptNative());

        // required accessor: System
        registerPackagedFunction(new ExitNative());
        registerPackagedFunction(new CheckNativeTypeNative());
        registerPackagedFunction(new LoadNativeLibraryNative());
        registerPackagedFunction(new CurrentThreadNameNative());

        // required accessor: Date
        registerPackagedFunction(new GetWeekDayNative());
        registerPackagedFunction(new GetDayNative());
        registerPackagedFunction(new GetMonthNative());
        registerPackagedFunction(new GetYearNative());
    }

    private void initializeNativeClasses() {
        registerNativeClass(new MathNativeClass());
        registerNativeClass(new ReflectionsNativeClass());
        registerNativeClass(new FileNativeClass());
    }

    private void registerDefaultFunction(final DefaultNativeFunction<?> function) {
        defaultFunctions.put(function.getName(), function);
    }

    private void registerPackagedFunction(final PackagedNativeFunction<?> function) {
        packagedFunctions.put(function.getFullyQualifiedName(), function);
    }

    private void registerNativeClass(final NativeClass clazz) {
        nativeClasses.put(clazz.getName(), clazz);
    }

    public Object callDefault(final String name, final List<Object> params) {
        final ClarityNativeFunction<?> method = defaultFunctions.get(name);
        if (method != null && method.applies(name, params)) {
            return method.call(params);
        }

        Register.throwException("Default native function " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    public Object callPackaged(final String pkg, final String name, final String callerClass, final List<Object> params) {
        String key = pkg + "." + name;
        PackagedNativeFunction<?> function = packagedFunctions.get(key);
        if (function != null && function.applies(pkg, name, callerClass, params)) {
            return function.call(params);
        }

        Register.throwException("Packaged native function " + pkg + (pkg.endsWith(".") ? "" : ".") + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    public Object callClassNative(final String name, final String method, final List<Object> params, final Context context) {

        final ClarityNativeClass clazz = nativeClasses.get(name);
        if (clazz != null) {
            try {
                return clazz.handleCall(method, params, context);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
                return null;
            }
        }

        Register.throwException("Class native function " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    private String objectsToClassesString(final List<Object> objects) {
        StringBuilder s = new StringBuilder();
        for (int i = 0, objectsLength = objects.size(); i < objectsLength; i++) {
            if (i != 0) s.append(", ");
            s.append(objects.get(i).getClass().getSimpleName().toLowerCase());
        }
        return s.toString();
    }

    public static void loadLibrary(final ClarityNativeLibrary lib) {
        for (final ClarityNativeFunction<?> function : lib.getLibraryNativeFunctions()) {
            defaultFunctions.put(function.getName(), function);
        }

        for (final ClarityNativeClass clazz : lib.getLibraryNativeClasses()) {
            nativeClasses.put(clazz.getName(), clazz);
        }
    }
}
