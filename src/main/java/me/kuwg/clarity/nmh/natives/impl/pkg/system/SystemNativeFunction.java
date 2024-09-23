package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import me.kuwg.clarity.nmh.natives.abstracts.PackagedNativeFunction;

import java.util.List;

abstract class SystemNativeFunction<R> extends PackagedNativeFunction<R> {
    protected SystemNativeFunction(final String name) {
        super(name);
    }

    @Override
    protected final boolean canCall(final String className) {
        return "System".equals(className);
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }
}
