package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.*;

import static me.kuwg.clarity.interpreter.types.VoidObject.VOID_OBJECT;

public class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();
    private final List<String> natives = new ArrayList<>();

    private String currentClassName;
    private String currentFunctionName;

    private final Context parentContext;

    public Context(final Context parentContext) {
        this.parentContext = parentContext;
    }

    public Context() {
        this(null);
    }

    public void defineVariable(final String name, final VariableDefinition value) {
        if (variables.putIfAbsent(name, value) != null) {
            Register.throwException("Declaring an already declared variable: " + name);
        }
    }

    public Object getVariable(final String name) {
        final ObjectType result = variables.get(name);
        if (result == null && parentContext != null) {
            return parentContext.getVariable(name);
        }
        return result != null ? ((VariableDefinition) result).getValue() : VOID_OBJECT;
    }

    public ObjectType getVariableDefinition(final String name) {
        final ObjectType result = variables.get(name);
        return result != null ? result : (parentContext != null ? parentContext.getVariableDefinition(name) : VOID_OBJECT);
    }

    public void setVariable(final String name, final Object value) {
        final ObjectType definition = getVariableDefinition(name);
        if (!(definition instanceof VariableDefinition)) {
            Register.throwException("You cannot edit a variable that hasn't been created: " + name);
            return;
        }
        final VariableDefinition variableDefinition = (VariableDefinition) definition;
        if (variableDefinition.isConstant() && variableDefinition.getValue() != VOID_OBJECT) {
            Register.throwException("Variable that has const cannot be edited: " + name);
        }
        variableDefinition.setValue(value);
    }

    public void defineFunction(final String name, final FunctionDefinition definition) {
        final List<FunctionDefinition> existingDefinitions = functions.computeIfAbsent(name, k -> new ArrayList<>());
        for (final FunctionDefinition d : existingDefinitions) {
            if (d.getParams().size() == definition.getParams().size()) {
                Register.throwException("Declaring an already declared function: " + name + " with the same number of parameters.");
                break;
            }
        }
        existingDefinitions.add(definition);
    }

    public ObjectType getFunction(final String name, final int paramsSize) {
        final List<FunctionDefinition> definitions = functions.get(name);
        if (definitions != null) {
            for (final FunctionDefinition d : definitions) {
                if (paramsSize == d.getParams().size()) {
                    return d;
                }
            }
        }
        return parentContext != null ? parentContext.getFunction(name, paramsSize) : VOID_OBJECT;
    }

    public void defineClass(final String name, final ClassDefinition definition) {
        if (classes.putIfAbsent(name, definition) != null) {
            Register.throwException("Declaring an already declared class: " + name);
        }
    }

    public ObjectType getClass(final String name) {
        if (name == null) return null;
        final ObjectType result = classes.get(name);
        return result != null ? result : (parentContext != null ? parentContext.getClass(name) : VOID_OBJECT);
    }

    public List<String> getNatives() {
        return new ArrayList<>(natives);
    }

    public String getCurrentClassName() {
        return currentClassName != null ? currentClassName : (parentContext != null ? parentContext.getCurrentClassName() : null);
    }

    public void setCurrentClassName(final String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public String getCurrentFunctionName() {
        return currentFunctionName != null ? currentFunctionName : (parentContext != null ? parentContext.getCurrentFunctionName() : null);
    }

    public void setCurrentFunctionName(final String currentFunctionName) {
        this.currentFunctionName = currentFunctionName;
    }

    public void mergeContext(final Context source) {
        if (source == null) return;

        source.variables.forEach(this.variables::putIfAbsent);

        source.functions.forEach((key, list) -> {
            final List<FunctionDefinition> targetFunctions = this.functions.computeIfAbsent(key, k -> new ArrayList<>());
            list.forEach(function -> {
                if (!targetFunctions.contains(function)) {
                    targetFunctions.add(function);
                }
            });
        });

        source.classes.forEach(this.classes::putIfAbsent);

        source.natives.forEach(nativeName -> {
            if (!this.natives.contains(nativeName)) {
                this.natives.add(nativeName);
            }
        });
    }

    public Context parentContext() {
        return parentContext;
    }

    @Override
    public String toString() {
        return String.format("Context{%n" +
                        "variables=%s,%n" +
                        "functions=%s,%n" +
                        "classes=%s,%n" +
                        "natives=%s,%n" +
                        "currentClassName='%s',%n" +
                        "currentFunctionName='%s',%n" +
                        "parentContext=%s%n" +
                        "}",
                variables, functions, classes, natives,
                currentClassName, currentFunctionName, parentContext);
    }
}