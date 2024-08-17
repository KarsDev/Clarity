package me.kuwg.clarity.cir.interpreter.context;

import me.kuwg.clarity.cir.interpreter.definition.CIRClassDefinition;
import me.kuwg.clarity.cir.interpreter.definition.CIRFunctionDefinition;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.Map;

import static me.kuwg.clarity.interpreter.types.Null.NULL;

public class CIRContext {

    private final Map<String, ObjectType> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();
    private final CIRContext parentContext;

    public CIRContext(CIRContext parentContext) {
        this.parentContext = parentContext;
    }

    public CIRContext() {
        this(null);
    }


    public void defineFunction(final String name, final CIRFunctionDefinition definition) {
        if (functions.containsKey(name)) {
            throw new IllegalStateException("Declaring an already declared function: " + name);
        }
        functions.put(name, definition);
    }

    public ObjectType getFunction(final String name) {
        final ObjectType result = functions.getOrDefault(name, NULL);
        if (result == NULL && parentContext != null) {
            return parentContext.getFunction(name);
        }
        return result;
    }

    public void defineClass(final String name, final CIRClassDefinition definition) {
        if (classes.containsKey(name)) {
            throw new IllegalStateException("Declaring an already declared class: " + name);
        }
        classes.put(name, definition);
    }

    public ObjectType getClass(final String name) {
        final ObjectType result = classes.getOrDefault(name, NULL);
        if (result == NULL && parentContext != null) {
            return parentContext.getClass(name);
        }
        return result;
    }

    public CIRContext parentContext() {
        return parentContext;
    }

    @Override
    public String toString() {
        return "Context{" +
                ", functions=" + functions +
                ", classes=" + classes +
                ", parentContext=" + parentContext +
                '}';
    }
}