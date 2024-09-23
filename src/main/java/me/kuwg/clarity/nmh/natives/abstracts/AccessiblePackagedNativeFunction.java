package me.kuwg.clarity.nmh.natives.abstracts;

public abstract class AccessiblePackagedNativeFunction<T> extends PackagedNativeFunction<T> {

    protected AccessiblePackagedNativeFunction(final String name) {
        super(name);
    }

    @Override
    protected final boolean canCall(final String className) {
        return true;
    }
}
