package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeMethod;

abstract class SystemNativeMethod<R> extends PackagedNativeMethod<R> {
    protected SystemNativeMethod(final String name) {
        super(name);
    }

    @Override
    protected final boolean canCall(final String className) {
        return "System".equals(className);
    }
}
