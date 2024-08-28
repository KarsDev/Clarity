package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import me.kuwg.clarity.interpreter.types.VoidObject;

import java.util.List;

public class ExitNative extends SystemNativeFunction<VoidObject> {
    public ExitNative() {
        super("exit");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        System.exit((int) params.get(0));
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 && params.get(0) instanceof Integer;
    }
}
