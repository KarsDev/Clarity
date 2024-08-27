package me.kuwg.clarity.interpreter.nmh.natives.io;

import me.kuwg.clarity.interpreter.nmh.natives.NativeMethod;
import me.kuwg.clarity.interpreter.types.VoidObject;

import java.util.List;

public class PrintlnNative extends NativeMethod<VoidObject> {

    public PrintlnNative() {
        super("println");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        switch (params.size()) {
            case 0:
                System.out.println();
                return VOID_OBJECT;
            case 1:
                System.out.println(params.get(0));
                return VOID_OBJECT;
            default:
                System.out.println(params);
                return VOID_OBJECT;
        }
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return true;
    }
}
