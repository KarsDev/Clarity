package me.kuwg.clarity.nmh.natives.impl.pkg.error;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.nmh.natives.abstracts.AccessiblePackagedNativeFunction;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class ThrowNative extends AccessiblePackagedNativeFunction<VoidObject> {
    public ThrowNative() {
        super("throw");
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

}
