package me.kuwg.clarity.nmh.natives.aclass;

import java.util.List;

public abstract class PackagedNativeMethod<R> extends NativeMethod<R> {
    private final String pkg;
    protected PackagedNativeMethod(final String name) {
        super(name);
        this.pkg = getClass().getPackage().getName().substring(37);
    }

    public final boolean applies(final String pkg, final String name, final List<Object> params) {
        return pkg.equals(this.pkg) && applies(name, params);
    }

    @Override
    public boolean applies(final String name, final List<Object> params) {
        return super.applies(name, params);
    }
}