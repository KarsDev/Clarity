package me.kuwg.clarity.library.cnc;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.register.Register;

import java.util.List;

public abstract class ClarityNativeClass {
    protected static final VoidObject VOID = VoidObject.VOID_OBJECT;

    private final String name;

    protected ClarityNativeClass(final String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract Object handleCall(final String name, final List<Object> params, final Context context) throws Exception;

    protected final void check(final String err, final boolean... condos) {
        for (final boolean condo : condos) {
            if (!condo) {
                Register.throwException(err);
            }
        }
    }

    protected final String getParamTypes(List<Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (sb.length() > 0) sb.append(", ");
            if (param == null) sb.append("null");
            else if (param instanceof Integer) sb.append("int");
            else if (param instanceof Double) sb.append("float");
            else if (param instanceof String) sb.append("str");
            else if (param instanceof Object[]) sb.append("arr");
            else sb.append(param.getClass().getSimpleName());
        }
        return sb.toString();
    }
}