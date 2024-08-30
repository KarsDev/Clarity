package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.Arrays;
import java.util.List;

public class PrintlnNative extends DefaultNativeFunction<VoidObject> {

    public PrintlnNative() {
        super("println");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        System.out.println(paramsToString(params));
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }

    private String paramsToString(final List<Object> params) {
        if (params.size() == 1)
            if (params.get(0) instanceof Object[]) return Arrays.toString((Object[]) params.get(0));

        StringBuilder s = new StringBuilder();
        for (final Object param : params) {
            if (param instanceof Object[]) s.append(Arrays.toString((Object[]) param));
            else s.append(param);
        }
        return s.toString();
    }
}
