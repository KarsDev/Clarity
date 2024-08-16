package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.Map;

import static me.kuwg.clarity.interpreter.types.Null.NULL;

public class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, ObjectType> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();

    public void defineVariable(final String name, final VariableDefinition value) {
        variables.put(name, value);
    }

    public Object getVariable(final String name) {
        final ObjectType result = variables.getOrDefault(name, NULL);
        return result == NULL ? NULL : ((VariableDefinition) result).getValue();
    }

    public void defineFunction(final String name, final FunctionDefinition definition) {
        functions.put(name, definition);
    }

    public ObjectType getFunction(final String name) {
        return functions.getOrDefault(name, NULL);
    }

    public void defineClass(final String name, final ClassDefinition definition) {
        classes.put(name, definition);
    }

    public ObjectType getClass(final String name) {
        return classes.getOrDefault(name, NULL);
    }
}
