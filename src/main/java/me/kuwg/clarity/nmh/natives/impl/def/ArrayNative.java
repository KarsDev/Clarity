package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class ArrayNative extends DefaultNativeFunction<Object[]> {
    public ArrayNative() {
        super("array");
    }

    @Override
    public Object[] call(final List<Object> params) {
        try {
            return new Object[((Long) params.get(0)).intValue()];
        } catch (final Exception e) {
            Register.throwException("Unexpected error while creating array with size: " + params.get(0));
            return new Object[]{VOID};
        }
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 && params.get(0) instanceof Long;
    }
}