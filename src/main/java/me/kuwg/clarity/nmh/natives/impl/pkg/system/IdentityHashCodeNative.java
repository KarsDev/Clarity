package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import java.util.List;

public class IdentityHashCodeNative extends SystemNativeFunction<Long> {

    public IdentityHashCodeNative() {
        super("identityHashCode");
    }

    @Override
    public Long call(final List<Object> params) {
        return (long) System.identityHashCode(params.get(0));
    }
}
