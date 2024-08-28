package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.interpreter.register.Register;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.*;

import static me.kuwg.clarity.interpreter.types.VoidObject.VOID;

public class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();
    private final List<String> natives = new ArrayList<>();

    private String currentClassName, currentFunctionName;

    private final Context parentContext;

    public Context(Context parentContext) {
        this.parentContext = parentContext;
    }

    public Context() {
        this(null);
    }

    public void defineVariable(String name, VariableDefinition value) {
        if (variables.putIfAbsent(name, value) != null) {
            throw new IllegalStateException("Declaring an already declared variable: " + name);
        }
    }

    public Object getVariable(String name) {
        ObjectType result = variables.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getVariable(name);
        }
        return result == VOID ? VOID : ((VariableDefinition) result).getValue();
    }

    public ObjectType getVariableDefinition(String name) {
        ObjectType result = variables.getOrDefault(name, VOID);
        if (result == VOID && parentContext != null) {
            return parentContext.getVariableDefinition(name);
        }
        return result;
    }

    public void setVariable(String name, Object value) {
        ObjectType definition = getVariableDefinition(name);
        if (!(definition instanceof VariableDefinition)) {
            Register.throwException("You cannot edit a variable that hasn't been created: " + name);
            return;
        }
        final VariableDefinition variableDefinition = ((VariableDefinition) definition);
        if (variableDefinition.isConstant()) Register.throwException("Variable that has const cannot be edited");
        variableDefinition.setValue(value);
    }

    public void defineFunction(String name, FunctionDefinition definition) {
        functions.computeIfAbsent(name, k -> new ArrayList<>()).add(definition);
    }

    public ObjectType getFunction(String name, int paramsSize) {
        List<FunctionDefinition> definitions = functions.get(name);
        if (definitions != null) {
            for (FunctionDefinition d : definitions) {
                if (paramsSize == d.getParams().size()) {
                    return d;
                }
            }
            return parentContext != null ? parentContext.getFunction(name, paramsSize) : VOID;
        }
        return parentContext != null ? parentContext.getFunction(name, paramsSize) : VOID;
    }

    public void defineClass(String name, ClassDefinition definition) {
        if (classes.putIfAbsent(name, definition) != null) {
            throw new IllegalStateException("Declaring an already declared class: " + name);
        }
    }

    public ObjectType getClass(String name) {
        ObjectType result = classes.getOrDefault(name, VOID);
        return result == VOID && parentContext != null ? parentContext.getClass(name) : result;
    }


    public final List<String> getNatives() {
        return natives;
    }

    public String getCurrentClassName() {
        return currentClassName != null ? currentClassName : (parentContext != null ? parentContext.getCurrentClassName() : null);
    }

    public void setCurrentClassName(String currentClassName) {
        this.currentClassName = currentClassName;
    }

    public String getCurrentFunctionName() {
        return currentFunctionName != null ? currentFunctionName : (parentContext != null ? parentContext.getCurrentFunctionName() : null);
    }

    public void setCurrentFunctionName(String currentFunctionName) {
        this.currentFunctionName = currentFunctionName;
    }

    public Context parentContext() {
        return parentContext;
    }
}
