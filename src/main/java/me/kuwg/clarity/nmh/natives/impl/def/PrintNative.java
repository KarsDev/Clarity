package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeMethod;

import java.util.Arrays;
import java.util.List;

public class PrintNative extends DefaultNativeMethod<VoidObject> {
    public PrintNative() {
        super("print");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        System.out.print(paramsToString(params));
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }

    private String paramsToString(final List<Object> params) {
        StringBuilder s = new StringBuilder();
        for (final Object param : params) {
            if (param instanceof Object[]) s.append(Arrays.toString((Object[]) param));
            else s.append(param);
        }
        return s.toString();
    }
}