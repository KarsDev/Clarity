package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeMethod;

import java.util.List;

public class NowNative extends DefaultNativeMethod<Long> {
    public NowNative() {
        super("now");
    }

    @Override
    public Long call(final List<Object> params) {
        return System.currentTimeMillis();
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.isEmpty();
    }
}
