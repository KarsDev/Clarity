package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.abstracts.NativeClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;

public class UnsafeNativeClass extends NativeClass {

    private static final Unsafe unsafe;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public UnsafeNativeClass() {
        super("unsafe");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) {
        switch (name) {
            case "allocateMemory": {
                check("Invalid parameters for 'allocateMemory'. Expected 1 int, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Long);
                final long size = (Long) params.get(0);
                return unsafe.allocateMemory(size);
            }
            case "freeMemory": {
                check("Invalid parameters for 'freeMemory'. Expected 1 int, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Long);
                final long address = (Long) params.get(0);
                unsafe.freeMemory(address);
                break;
            }
            case "getInt": {
                check("Invalid parameters for 'getInt'. Expected 1 int, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Long);
                final long address = (Long) params.get(0);
                return unsafe.getLong(address);
            }
            case "putInt": {
                check("Invalid parameters for 'putInt'. Expected 2 parameters (int, int), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof Long && params.get(1) instanceof Long);
                final long address = (Long) params.get(0);
                final long value = (Integer) params.get(1);
                unsafe.putLong(address, value);
                break;
            }
            case "getBool": {
                check("Invalid parameters for 'getBool'. Expected 2 parameters (int), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Long);
                final long address = (Long) params.get(0);
                return unsafe.getByte(address) == 1;
            }
            case "putBool": {
                check("Invalid parameters for 'putBool'. Expected 3 parameters (int, bool), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof Long && params.get(1) instanceof Boolean);
                final long address = (Long) params.get(1);
                final boolean value = (Boolean) params.get(1);
                unsafe.putByte(address, (byte) (value ? 1 : 0));
                break;
            }
            case "getFloat": {
                check("Invalid parameters for 'getFloat'. Expected 2 parameters (int), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Long);
                final long address = (Long) params.get(0);
                return unsafe.getDouble(address);
            }
            case "putFloat": {
                check("Invalid parameters for 'putFloat'. Expected 3 parameters (int, float), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof Long && params.get(1) instanceof Double);
                final long address = (Long) params.get(0);
                final double value = (Double) params.get(1);
                unsafe.putDouble(address, value);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported unsafe operation: " + name);
            }
        }

        return VOID;
    }

    private void check(final String message, final boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}