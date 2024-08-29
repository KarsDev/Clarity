package me.kuwg.clarity.nmh.natives.aclass;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.interpreter.types.VoidObject;

import java.util.List;

public abstract class NativeClass {

    protected static final VoidObject VOID = VoidObject.VOID_OBJECT;

    private final String name;

    protected NativeClass(final String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract Object handleCall(final String name, final List<Object> params, final Context context) throws Exception;

    protected final void check(final String err, final boolean... condos) {
        for (final boolean condo : condos) if (!condo) Register.throwException(err);
    }
}
