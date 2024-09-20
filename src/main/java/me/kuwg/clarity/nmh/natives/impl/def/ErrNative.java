package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.Arrays;
import java.util.List;

public class ErrNative extends DefaultNativeFunction<VoidObject> {

    public ErrNative() {
        super("err");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        System.err.println(paramsToString(params));
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 || (params.size() == 2 && params.get(1) instanceof Boolean);
    }

    private String paramsToString(final List<Object> params) {
        final String line = params.get(0) instanceof Object[] ? Arrays.toString((Object[]) params.get(0)) : params.get(0).toString();
        return params.size() == 1 ? line : (boolean) params.get(1) ? line + "\n" : line;
    }
}
