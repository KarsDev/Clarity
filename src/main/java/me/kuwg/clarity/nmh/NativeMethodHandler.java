package me.kuwg.clarity.nmh;

import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeMethod;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeMethod;
import me.kuwg.clarity.nmh.natives.impl.def.InputNative;
import me.kuwg.clarity.nmh.natives.impl.def.NowNative;
import me.kuwg.clarity.nmh.natives.impl.def.PrintNative;
import me.kuwg.clarity.nmh.natives.impl.def.PrintlnNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.error.ExceptNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.ExitNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.util.CreateListNative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeMethodHandler {

    private final Map<String, DefaultNativeMethod<?>> defaultMethods = new HashMap<>();
    private final Map<String, PackagedNativeMethod<?>> packagedMethods = new HashMap<>();

    public NativeMethodHandler() {
        initializeDefaultMethods();
        initializePackagedMethods();
    }

    private void initializeDefaultMethods() {
        registerDefaultMethod(new PrintlnNative());
        registerDefaultMethod(new InputNative());
        registerDefaultMethod(new PrintNative());
        registerDefaultMethod(new NowNative());
    }

    private void initializePackagedMethods() {
        registerPackagedMethod(new CreateListNative());
        registerPackagedMethod(new ExceptNative());
        registerPackagedMethod(new ExitNative());
    }

    private void registerDefaultMethod(DefaultNativeMethod<?> method) {
        defaultMethods.put(method.getName(), method);
    }

    private void registerPackagedMethod(PackagedNativeMethod<?> method) {
        packagedMethods.put(method.getFullyQualifiedName(), method);
    }

    public Object callDefault(final String name, final List<Object> params) {
        DefaultNativeMethod<?> method = defaultMethods.get(name);
        if (method != null && method.applies(name, params)) {
            return method.call(params);
        }

        Register.throwException("Default native method " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    public Object callPackaged(final String pkg, final String name, final String callerClass, final List<Object> params) {
        String key = pkg + "." + name;
        PackagedNativeMethod<?> method = packagedMethods.get(key);
        if (method != null && method.applies(pkg, name, callerClass, params)) {
            return method.call(params);
        }

        Register.throwException("Packaged native method " + pkg + "." + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
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
