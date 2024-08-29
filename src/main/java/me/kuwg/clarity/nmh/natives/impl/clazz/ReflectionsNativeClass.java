package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.interpreter.types.ObjectType;
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
                check("Invalid params", params.size() == 2 && params.get(0) instanceof String);
                final String var = (String) params.get(0);
                final Object val = params.get(1);

                try {
                    ((VariableDefinition) context.getVariableDefinition(var)).setValue(val);
                } catch (final ClassCastException e) {
                    Register.throwException("Editing a non created variable: " + var);
                }
                break;
            }
            case "createVariable": {
                check("Invalid params", params.size() == 4 && params.get(0) instanceof String && params.get(2) instanceof Boolean && params.get(3) instanceof Boolean);
                final String var = (String) params.get(0);
                final Object val = params.get(1);
                final boolean isStatic = (Boolean) params.get(2);
                final boolean isConstant = (Boolean) params.get(3);

                context.parentContext().defineVariable(var, new VariableDefinition(var, val, isStatic, isConstant));
                break;
            }
            case "getCallerClass": {
                check("Invalid params", params.isEmpty());
                final Register.RegisterElement element = Register.getStack().size() > 3 ? Register.getStack().at(Register.getStack().size() - 4) : null;
                if (element == null) return null;
                return element.getCurrentClass();
            }
        }

        return VOID;
    }
}
