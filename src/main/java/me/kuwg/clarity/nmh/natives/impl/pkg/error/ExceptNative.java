package me.kuwg.clarity.nmh.natives.impl.pkg.error;

import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.PackagedNativeMethod;

import java.util.List;

public class ExceptNative extends PackagedNativeMethod<VoidObject> {
    public ExceptNative() {
        super("except");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        final String error = params.get(0).toString();
        final String line = params.size() == 2 ? params.get(1).toString() : "?";
        Register.throwException(error, line);
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return (params.size() == 1 && (params.get(0) instanceof String || params.get(0) instanceof Number)) ||
               ((params.size() == 2 && (params.get(0) instanceof String || params.get(0) instanceof Number)) && params.get(1) instanceof Number);
    }
}
