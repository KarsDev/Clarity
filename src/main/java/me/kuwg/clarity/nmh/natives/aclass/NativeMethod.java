package me.kuwg.clarity.nmh.natives.aclass;

import me.kuwg.clarity.interpreter.types.VoidObject;

import java.util.List;

abstract class NativeMethod<R> {

    protected static final VoidObject VOID = VoidObject.VOID;

    private final String name;

    protected NativeMethod(final String name) {
        this.name = name;
    }

    public abstract R call(final List<Object> params);

    protected abstract boolean applies0(final List<Object> params);

    public boolean applies(final String name, final List<Object> params) {
        return this.name.equals(name) && applies0(params);
    }

    public final String getName() {
        return name;
    }
}
