package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.library.objects.ObjectType;
import me.kuwg.clarity.nmh.natives.abstracts.NativeClass;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class ReflectionsNativeClass extends NativeClass {
    public ReflectionsNativeClass() {
        super("Reflections");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) {
        switch (name) {
            case "getCallerClass": {
                final Register.RegisterElement element = Register.getStack().size() > 3 ? Register.getStack().at(Register.getStack().size() - 4) : null;
                if (element == null) return null;
                return context.getCurrentClassName();
            }
            case "isNative": {
                exceptIf("'name' must be str type.",
                        params.get(1) instanceof String);
                final String className = (String) params.get(1);
                final ObjectType rawClass = context.getClass(className);

                if (!(rawClass instanceof ClassDefinition)) {
                    return except("Class not found or loaded: " + className);
                }

                final ClassDefinition definition = (ClassDefinition) rawClass;

                return definition.isNative();
            }

            default: {
                return except("Unsupported method name: " + name);
            }
        }
    }


    private Object except(String message) {
        Register.throwException(message);
        return VOID;
    }

    private void exceptIf(String message, boolean condition) {
        if (!condition) {
            except(message);
        }
    }
}
