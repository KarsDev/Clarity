package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void help() {
        final Map<String, String> map = new HashMap<>();

        System.out.println(formatHelp(map));
    }
}
