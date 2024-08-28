package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;

public class NanoNative extends DefaultNativeFunction<Long> {
    public NanoNative() {
        super("nano");
    }

    @Override
    public Long call(final List<Object> params) {
        return System.nanoTime();
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.isEmpty();
    }
}
