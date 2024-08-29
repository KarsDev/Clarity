package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;

public class NowNative extends DefaultNativeFunction<Long> {
    private static final long START = System.currentTimeMillis();
    public NowNative() {
        super("now");
    }

    @Override
    public Long call(final List<Object> params) {
        return System.currentTimeMillis() - START;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.isEmpty();
    }
}
