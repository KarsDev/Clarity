package me.kuwg.clarity.nmh;

import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeMethod;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeMethod;
import me.kuwg.clarity.nmh.natives.impl.def.InputNative;
import me.kuwg.clarity.nmh.natives.impl.def.PrintNative;
import me.kuwg.clarity.nmh.natives.impl.def.PrintlnNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.error.ExceptNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.system.ExitNative;
import me.kuwg.clarity.nmh.natives.impl.pkg.util.CreateListNative;

import java.util.List;

public class NativeMethodHandler {

    private final DefaultNativeMethod<?>[] DEFAULT = new DefaultNativeMethod[]{
            new PrintlnNative(),
            new InputNative(),
            new PrintNative(),


    };

    private final PackagedNativeMethod<?>[] PACKAGED = new PackagedNativeMethod[]{
            new CreateListNative(),
            new ExceptNative(),
            new ExitNative()


    };

    public Object callDefault(final String name, final List<Object> params) {
        for (final DefaultNativeMethod<?> method : this.DEFAULT) {
            if (method.applies(name, params)) {
                return method.call(params);
            }
        }

        Register.throwException("Default native method " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
        return null;
    }

    public Object callPackaged(final String pkg, final String name, final String callerClass, final List<Object> params) {
        for (final PackagedNativeMethod<?> method : this.PACKAGED) {
            if (method.applies(pkg, name, callerClass, params)) {
                return method.call(params);
            }
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
