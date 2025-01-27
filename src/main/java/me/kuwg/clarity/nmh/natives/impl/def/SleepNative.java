package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepNative extends DefaultNativeFunction<VoidObject> {
    public SleepNative() {
        super("sleep");
    }

    @Override
    public VoidObject call(final List<Object> list) {
        try {
            Thread.sleep((long) list.get(0));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> list) {
        return list.size() == 1 && list.get(0) instanceof Long;
    }

    @Override
    public void help() {
        final Map<String, String> map = new HashMap<>();

        map.put("time", "int");

        System.out.println(formatHelp(map));
    }
}
