package me.kuwg.clarity.nmh;

import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;
import me.kuwg.clarity.nmh.natives.aclass.NativeClass;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeFunction;
import me.kuwg.clarity.nmh.natives.impl.clazz.MathNativeClass;
import me.kuwg.clarity.nmh.natives.impl.def.*;
import me.kuwg.clarity.nmh.natives.impl.pkg.error.ExceptNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.ExitNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.util.CreateListNative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeMethodHandler {

    private final Map<String, DefaultNativeFunction<?>> defaultFunctions = new HashMap<>();
    private final Map<String, PackagedNativeFunction<?>> packagedFunctions = new HashMap<>();
    private final Map<String, NativeClass> nativeClasses = new HashMap<>();

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
    }

    private void initializePackagedFunctions() {
        registerPackagedFunction(new CreateListNative());
        registerPackagedFunction(new ExceptNative());
        registerPackagedFunction(new ExitNative());
    }

    private void initializeNativeClasses() {
        registerNativeClass(new MathNativeClass());
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
        DefaultNativeFunction<?> method = defaultFunctions.get(name);
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

        Register.throwException("Packaged native function " + pkg +(pkg.endsWith(".") ? "" : ".") + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    public Object callClassNative(final String name, final String method, final List<Object> params) {
        NativeClass clazz = nativeClasses.get(name);
        if (clazz != null) {
            return clazz.handleCall(method, params);
        }

        Register.throwException("Default native function " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
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
}
