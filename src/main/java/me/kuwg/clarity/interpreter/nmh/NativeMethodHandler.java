package me.kuwg.clarity.interpreter.nmh;

import me.kuwg.clarity.interpreter.nmh.natives.NativeMethod;
import me.kuwg.clarity.interpreter.nmh.natives.io.InputNative;
import me.kuwg.clarity.interpreter.nmh.natives.io.PrintlnNative;

import java.util.List;

public class NativeMethodHandler {

    private final NativeMethod<?>[] methods = new NativeMethod[] {
        new PrintlnNative(),
        new InputNative()
    };

    public Object call(final String name, final List<Object> params) {
        for (final NativeMethod<?> method : this.methods) {
            if (method.applies(name, params)) {
                return method.call(params);
            }
        }

        throw new UnsupportedOperationException("Native method " + name + "(" + objectsToClassesString(params) + ") not found or not accessible.");
    }


    private String objectsToClassesString(final Object... objects) {
        StringBuilder s = new StringBuilder();
        for (int i = 0, objectsLength = objects.length; i < objectsLength; i++) {
            if (i != 0) s.append(", ");
            s.append(objects[i].getClass().getSimpleName());
        }
        return s.toString();
    }
}
