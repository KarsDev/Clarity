package me.kuwg.clarity.nmh.natives.impl.pkg.error;

import me.kuwg.clarity.library.VoidObject;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeFunction;

import java.util.List;

public class ExceptNative extends PackagedNativeFunction<VoidObject> {
    public ExceptNative() {
        super("except");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        final String error = params.get(0).toString();
        if (params.size() == 2) Register.throwException(error, params.get(1).toString());
        else Register.throwException(error);
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return (params.size() == 1 && (params.get(0) instanceof String || params.get(0) instanceof Number)) ||
               ((params.size() == 2 && (params.get(0) instanceof String || params.get(0) instanceof Number)) && params.get(1) instanceof Number);
    }

    @Override
    protected boolean canCall(final String className) {
        return true;
    }
}
