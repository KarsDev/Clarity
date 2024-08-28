package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.nmh.natives.aclass.NativeClass;

import java.util.List;

public class MathNativeClass extends NativeClass {
    public MathNativeClass() {
        super("Math");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params) {
        switch (name) {
            case "sqrt": {
                check("?", params.size() == 1 && params.get(0) instanceof Number);
                return Math.sqrt(((Number) params.get(0)).doubleValue());
            }
            case "cbrt": {
                check("?", params.size() == 1 && params.get(0) instanceof Number);
                return Math.cbrt(((Number) params.get(0)).doubleValue());
            }
        }

        throw new UnsupportedOperationException("Unsupported math native: " + name);
    }
}
