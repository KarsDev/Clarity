package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kuwg.clarity.interpreter.types.VoidObject.VOID;

public class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();

    private boolean isNativeClass;
    private String currentClassName, currentFunctionName;

    private final Context parentContext;

    public Context(Context parentContext) {
        this.parentContext = parentContext;
    }

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

    public ObjectType getVariableDefinition(final String name) {
        final ObjectType result = variables.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getVariableDefinition(name);
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
        functions.computeIfAbsent(name, k -> new ArrayList<>()).add(definition);
    }

    public ObjectType getFunction(final String name, final int paramsSize) {
        List<FunctionDefinition> definitions = functions.get(name);
        if (definitions != null) {
            for (FunctionDefinition definition : definitions) {
                if (paramsSize == definition.getParams().size()) {
                    return definition;
                }
            }
        }
        if (parentContext != null) {
            return parentContext.getFunction(name, paramsSize);
        }
        return VOID;
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

    public final boolean isNativeClass() {
        return isNativeClass;
    }

    public final void setNativeClass(final boolean nativeClass) {
        isNativeClass = nativeClass;
    }

    public final String getCurrentClassName() {
        if (currentClassName == null && parentContext != null) return parentContext.getCurrentClassName();
        return currentClassName;
    }

    public final void setCurrentClassName(final String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public final String getCurrentFunctionName() {
        return currentFunctionName == null && parentContext != null ? parentContext.currentFunctionName : getCurrentFunctionName();
    }

    public final void setCurrentFunctionName(final String currentFunctionName) {
        this.currentFunctionName = currentFunctionName;
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
                ", currentClassName='" + currentClassName + '\'' +
                ", currentFunctionName='" + currentFunctionName + '\'' +
                ", parentContext=" + parentContext +
                '}';
    }
}
