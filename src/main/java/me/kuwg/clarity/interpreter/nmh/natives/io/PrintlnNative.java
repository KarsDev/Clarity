package me.kuwg.clarity.interpreter.nmh.natives.io;

import me.kuwg.clarity.interpreter.nmh.natives.NativeMethod;
import me.kuwg.clarity.interpreter.types.Null;

import java.util.Arrays;
import java.util.List;

public class PrintlnNative extends NativeMethod<Null> {

    public PrintlnNative() {
        super("println");
    }

    @Override
    public Null call(final List<Object> params) {
        switch (params.size()) {
            case 0:
                System.out.println();
                return NULL;
            case 1:
                System.out.println(params.get(0));
                return NULL;
            default:
                System.out.println(params);
                return NULL;
        }
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }
}
