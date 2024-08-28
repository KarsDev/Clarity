package me.kuwg.clarity.nmh.natives.impl.pkg.util;

import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeFunction;

import java.util.List;

public class CreateListNative extends PackagedNativeFunction<Object[]> {
    public CreateListNative() {
        super("list");
    }

    @Override
    public Object[] call(final List<Object> params) {
        return params.toArray();
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return !params.isEmpty();
    }

    @Override
    protected boolean canCall(final String className) {
        return true;
    }
}
