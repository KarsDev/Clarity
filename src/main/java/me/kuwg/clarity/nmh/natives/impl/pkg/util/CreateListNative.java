package me.kuwg.clarity.nmh.natives.impl.pkg.util;

import me.kuwg.clarity.nmh.natives.abstracts.AccessiblePackagedNativeFunction;
import me.kuwg.clarity.nmh.natives.abstracts.PackagedNativeFunction;

import java.util.List;

public class CreateListNative extends AccessiblePackagedNativeFunction<Object[]> {
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
}
