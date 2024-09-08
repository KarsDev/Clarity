package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.VoidObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;

public class SleepNative extends DefaultNativeFunction<VoidObject> {
    public SleepNative() {
        super("sleep");
    }

    @Override
    public VoidObject call(final List<Object> list) {
        try {
            Thread.sleep((int) list.get(0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> list) {
        return list.size() == 1 && list.get(0) instanceof Integer;
    }
}
