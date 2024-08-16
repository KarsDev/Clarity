package me.kuwg.clarity.interpreter.nmh.natives;

import me.kuwg.clarity.interpreter.types.Null;

import java.util.List;

public abstract class NativeMethod<R> {

    protected static final Null NULL = Null.NULL;

    private final String name;

    protected NativeMethod(final String name) {
        this.name = name;
    }

    public abstract R call(final List<Object> params);

    protected abstract boolean applies0(final List<Object> params);

    public final boolean applies(final String name, final List<Object> params) {
        return this.name.equals(name) && applies0(params);
    }
}
