package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.Map;

import static me.kuwg.clarity.interpreter.types.VoidObject.VOID;

public class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, ObjectType> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();
    private final Context parentContext; // New parent context field

    // Constructor to initialize with a parent context
    public Context(Context parentContext) {
        this.parentContext = parentContext;
    }

    // Default constructor for a root context with no parent
    public Context() {
        this(null);
    }

    public void defineVariable(final String name, final VariableDefinition value) {
        if (variables.containsKey(name)) {
            throw new IllegalStateException("Declaring an already declared variable: " + name);
        }
        variables.put(name, value);
    }

    public Object getVariable(final String name) {
        final ObjectType result = variables.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getVariable(name);
        }
        return result == VOID ? VOID : ((VariableDefinition) result).getValue();
    }

    public Object getVariableDefinition(final String name) {
        final ObjectType result = variables.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getVariable(name);
        }
        return result;
    }

    public void setVariable(final String name, final Object value) {
        try {
            ((VariableDefinition) getVariableDefinition(name)).setValue(value);
        } catch (final ClassCastException e) {
            throw new IllegalStateException("You cannot edit a variable that hasn't been created: " + name);
        }
    }

    public void defineFunction(final String name, final FunctionDefinition definition) {
        if (functions.containsKey(name)) {
            throw new IllegalStateException("Declaring an already declared function: " + name);
        }
        functions.put(name, definition);
    }

    public ObjectType getFunction(final String name) {
        final ObjectType result = functions.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getFunction(name);
        }
        return result;
    }

    public void defineClass(final String name, final ClassDefinition definition) {
        if (classes.containsKey(name)) {
            throw new IllegalStateException("Declaring an already declared class: " + name);
        }
        classes.put(name, definition);
    }

    public ObjectType getClass(final String name) {
        final ObjectType result = classes.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getClass(name);
        }
        return result;
    }

    public Context parentContext() {
        return parentContext;
    }

    @Override
    public String toString() {
        return "Context{" +
                "variables=" + variables +
                ", functions=" + functions +
                ", classes=" + classes +
                ", parentContext=" + parentContext +
                '}';
    }
}