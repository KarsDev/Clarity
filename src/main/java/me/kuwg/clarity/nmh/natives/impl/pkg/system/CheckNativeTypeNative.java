package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import me.kuwg.clarity.register.Register;

import java.util.List;

public class CheckNativeTypeNative extends SystemNativeFunction<Boolean> {
    public CheckNativeTypeNative() {
        super("checkNativeType");
    }

    @Override
    public Boolean call(final List<Object> params) {
        if (!(params.get(0) instanceof String)) {
            Register.throwException("Native type must be string");
            return null;
        }

        final String required = (String) params.get(0);
        final Object got = params.get(1);

        switch (required) {
            case "str":
                return got instanceof String;
            case "float":
                return got instanceof Double;
            case "int":
                return got instanceof Integer;
            case "bool":
                return got instanceof Boolean;
            default:
                Register.throwException("Unknown native type: " + required);
        }

        return false;
    }

}
