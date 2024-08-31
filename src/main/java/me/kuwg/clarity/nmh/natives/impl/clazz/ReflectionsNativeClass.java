package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.nmh.natives.aclass.NativeClass;

import java.util.List;

public class ReflectionsNativeClass extends NativeClass {
    public ReflectionsNativeClass() {
        super("Reflections");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) {
        switch (name) {
            case "editVariable": {
                check("Invalid parameters for 'editVariable'. Expected 2 parameters (String variableName, Object value), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof String);
                final String var = (String) params.get(0);
                final Object val = params.get(1);

                try {
                    ((VariableDefinition) context.getVariableDefinition(var)).setValue(val);
                } catch (final ClassCastException e) {
                    Register.throwException("Error editing variable '" + var + "': " + e.getMessage());
                }
                break;
            }
            case "createVariable": {
                check("Invalid parameters for 'createVariable'. Expected 4 parameters (String variableName, Object initialValue, Boolean isStatic, Boolean isConstant), got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 4 && params.get(0) instanceof String && params.get(2) instanceof Boolean && params.get(3) instanceof Boolean);
                final String var = (String) params.get(0);
                final Object val = params.get(1);
                final boolean isStatic = (Boolean) params.get(2);
                final boolean isConstant = (Boolean) params.get(3);

                context.parentContext().defineVariable(var, new VariableDefinition(var, val, isStatic, isConstant));
                break;
            }
            case "getCallerClass": {
                check("Invalid parameters for 'getCallerClass'. Expected no parameters, got " + params.size() + " with types " + getParamTypes(params),
                        params.isEmpty());
                final Register.RegisterElement element = Register.getStack().size() > 3 ? Register.getStack().at(Register.getStack().size() - 4) : null;
                if (element == null) return null;
                return element.getCurrentClass();
            }
            default:
                Register.throwException("Unsupported method name: " + name);
        }

        return VOID;
    }

    private String getParamTypes(List<Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(param == null ? "null" : param.getClass().getSimpleName());
        }
        return sb.toString();
    }

    private void check(String message, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
