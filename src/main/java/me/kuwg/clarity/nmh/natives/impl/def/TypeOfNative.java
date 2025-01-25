package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.library.objects.types.LambdaObject;
import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;
import me.kuwg.clarity.register.Register;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        } else if (result instanceof Long) {
            return "int";
        } else if (result instanceof Double) {
            return "float";
        } else if (result instanceof ClassObject) {
            return ((ClassObject) result).getName();
        } else if (result instanceof Object[]) {
            return "arr";
        } else if (result instanceof Boolean) {
            return "bool";
        } else if (result instanceof LambdaObject) {
            return "lambda";
        }
        Register.throwException("Unknown type: " + result);
        return null;
    }

    @Override
    protected boolean applies0(final List<Object> list) {
        return list.size() == 1;
    }

    @Override
    public void help() {
        final Map<String, String> map = new HashMap<>();

        map.put("obj", "var");

        System.out.println(formatHelp(map));
    }
}
