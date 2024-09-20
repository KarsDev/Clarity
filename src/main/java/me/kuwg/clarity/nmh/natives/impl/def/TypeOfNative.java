package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.VoidObject;
import me.kuwg.clarity.interpreter.types.ClassObject;
import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class TypeOfNative extends DefaultNativeFunction<String> {
    public TypeOfNative() {
        super("typeof");
    }

    @Override
    public String call(final List<Object> list) {
        final Object result = list.get(0);
        if (result == null) {
            return "null";
        } else if (result instanceof VoidObject) {
            return "void";
        } else if (result instanceof String) {
            return "str";
        } else if (result instanceof Integer) {
            return "int";
        } else if (result instanceof Double) {
            return "float";
        } else if (result instanceof ClassObject) {
            return ((ClassObject) result).getName();
        } else if (result instanceof Object[]) {
            return "arr";
        } else if (result instanceof Boolean) {
            return "bool";
        }
        Register.throwException("Unknown type: " + result);
        return null;
    }

    @Override
    protected boolean applies0(final List<Object> list) {
        return list.size() == 1;
    }
}
