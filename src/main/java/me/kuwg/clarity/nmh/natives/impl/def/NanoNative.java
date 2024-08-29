package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;

public class NanoNative extends DefaultNativeFunction<Long> {
    private static final long START = System.nanoTime();
    public NanoNative() {
        super("nano");
    }

    @Override
    public Long call(final List<Object> params) {
        return System.nanoTime() - START;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.isEmpty();
    }
}
