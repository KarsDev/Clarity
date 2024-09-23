package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import me.kuwg.clarity.nmh.natives.abstracts.PackagedNativeFunction;

import java.util.List;

abstract class DateNativeFunction<R> extends PackagedNativeFunction<R> {
    protected DateNativeFunction(final String name) {
        super(name);
    }

    @Override
    protected final boolean canCall(final String className) {
        return "Date".equals(className);
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }
}
