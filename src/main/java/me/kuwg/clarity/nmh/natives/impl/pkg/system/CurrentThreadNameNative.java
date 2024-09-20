package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import java.util.List;

public class CurrentThreadNameNative extends SystemNativeFunction<String> {
    public CurrentThreadNameNative() {
        super("currentThreadName");
    }

    @Override
    public String call(final List<Object> params) {
        return Thread.currentThread().getName();
    }
}
