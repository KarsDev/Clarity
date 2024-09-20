package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;

public class PrintNative extends DefaultNativeFunction<VoidObject> {
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
            if (param instanceof Object[]) {
                s.append(arrayToString((Object[]) param));
            } else {
                s.append(param);
            }
            s.append(" ");
        }
        return s.toString().trim();
    }

    private String arrayToString(final Object[] array) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof Object[]) {
                s.append(arrayToString((Object[]) array[i]));
            } else {
                s.append(array[i]);
            }
            if (i < array.length - 1) {
                s.append(", ");
            }
        }
        s.append("]");
        return s.toString();
    }
}