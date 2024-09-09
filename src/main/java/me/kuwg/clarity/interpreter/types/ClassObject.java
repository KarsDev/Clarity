package me.kuwg.clarity.interpreter.types;

import me.kuwg.clarity.ObjectType;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;

public class ClassObject {

    private static Interpreter interpreter;

    public static void setInterpreter(final Interpreter interpreter) {
        ClassObject.interpreter = interpreter;
    }

    private final String name;
    private final ClassObject inherited;
    private final Context context;

    public ClassObject(final String name, final ClassObject inherited, final Context context) {
        this.name = name;
        this.inherited = inherited;
        this.context = context;
    }

    public final String getName() {
        return name;
    }

    public final ClassObject getInherited() {
        return inherited;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        final ObjectType type = context.getFunction("print", 0);
        if (type instanceof FunctionDefinition) {
            final Object ret = interpreter.interpretNode(((FunctionDefinition) type).getBlock(), context);
            if (ret instanceof String) {
                return (String) ret;
            }
        }
        return "ClassObject@" + name;
    }

    public boolean isInstance(final String value) {
        if (name.equals(value)) return true;
        ClassObject inherited = this.inherited;
        while (true) {
            if (inherited == null) return false;
            if (inherited.getName().equals(value)) return true;
            inherited = inherited.getInherited();
        }
    }
}
