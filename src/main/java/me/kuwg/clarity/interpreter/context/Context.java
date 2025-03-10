package me.kuwg.clarity.interpreter.context;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.interpreter.definition.AnnotationDefinition;
import me.kuwg.clarity.interpreter.definition.ClassDefinition;
import me.kuwg.clarity.interpreter.definition.FunctionDefinition;
import me.kuwg.clarity.interpreter.definition.VariableDefinition;
import me.kuwg.clarity.library.objects.ObjectType;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.util.StillTesting;

import java.util.*;

import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;

public final class Context {

    private final Map<String, ObjectType> variables = new HashMap<>();
    private final Map<String, List<FunctionDefinition>> functions = new HashMap<>();
    private final Map<String, ObjectType> classes = new HashMap<>();
    private final Map<String, ObjectType> annotations = new HashMap<>();
    private final Set<String> natives = new HashSet<>();
    private final List<String> currentAnnotationNames = new ArrayList<>();

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
        if (name.equals("_")) return;
        if (variables.putIfAbsent(name, value) != null) {
            Register.throwException("Declaring an already declared variable: " + name);
        }
    }

    public Object getVariable(final String name) {
        if (name.equals("_")) {
            Register.throwException("Getting a unnamed variable (_)");
            return VOID_OBJECT;
        }
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
        if (Interpreter.checkTypes(variableDefinition.getTypeDefault(), value)) {
            Register.throwException("Unexpected value for variable " + variableDefinition.getName() + ", expected " + variableDefinition.getTypeDefault() + " but got " + Interpreter.getAsCLRStr(value));
        }

        if (variableDefinition.isConstant() && variableDefinition.getValue() != VOID_OBJECT) {
            Register.throwException("Editing a constant variable: " + name);
        }
        variableDefinition.setValue(value);
    }

    public void deleteVariable(final String name) {
        if (variables.remove(name) == null) {
            Register.throwException("Attempted to delete non-existent variable: " + name);
        }
    }

    public void defineFunction(final String name, final FunctionDefinition definition) {
        final String currentClass = getCurrentClassName();

        if (currentClass != null) {
            ClassDefinition classDefinition = (ClassDefinition) getClass(currentClass);
            while (classDefinition.getInheritedClass() != null) {
                final ClassDefinition inherited = classDefinition.getInheritedClass();
                for (ASTNode node : inherited.getBody()) {
                    if (node instanceof FunctionDeclarationNode) {
                        final FunctionDeclarationNode fdn = (FunctionDeclarationNode) node;
                        if (fdn.getFunctionName().equals(name) &&
                                fdn.getParameterNodes().size() == definition.getParams().size() && fdn.isConst()) {
                            Register.throwException("Overriding const functions is not allowed", definition.getBlock().getLine());
                            return;
                        }
                    }
                }
                classDefinition = inherited;
            }
        }

        final List<FunctionDefinition> existingDefinitions = functions.computeIfAbsent(name, k -> new ArrayList<>());
        if (existingDefinitions.stream().anyMatch(d -> d.getParams().size() == definition.getParams().size())) {
            Register.throwException("Declaring an already declared function: " + name + " with the same number of parameters.");
            return;
        }

        existingDefinitions.add(definition);
    }

    public ObjectType getFunction(final String name, final int paramsSize) {
        try {
            final List<FunctionDefinition> definitions = functions.get(name);
            if (definitions != null) {
                for (final FunctionDefinition d : definitions) {
                    if (paramsSize == d.getParams().size()) {
                        return d;
                    }
                }
            }
            return parentContext != null ? parentContext.getFunction(name, paramsSize) : VOID_OBJECT;
        } catch (final VirtualMachineError ignore) {
            System.err.print("(Self Calling?) function error: " + name + " with " + paramsSize + " params.\n");
            Runtime.getRuntime().exit(666);
            throw new RuntimeException();
        }

    }

    public void deleteFunction(final String name, final int paramsSize) {
        final List<FunctionDefinition> definitions = functions.get(name);
        if (definitions != null) {
            boolean removed = definitions.removeIf(d -> d.getParams().size() == paramsSize);
            if (!removed) {
                Register.throwException("Attempted to delete non-existent function: " + name + " with " + paramsSize + " parameters.");
            } else if (definitions.isEmpty()) {
                functions.remove(name);
            }
        } else {
            Register.throwException("Attempted to delete non-existent function: " + name);
        }
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

    public void defineAnnotation(final String name, final AnnotationDefinition definition) {
        if (annotations.putIfAbsent(name, definition) != null) {
            Register.throwException("Declaring an already declared annotation: " + name);
        }
    }

    public ObjectType getAnnotation(final String name) {
        if (name == null) return null;
        final ObjectType result = annotations.getOrDefault(name, VOID_OBJECT);
        return result != VOID_OBJECT ? result : (parentContext != null ? parentContext.getAnnotation(name) : VOID_OBJECT);
    }

    public Set<String> getNatives() {
        return natives;
    }

    public String getCurrentClassName() {
        return currentClassName != null ? currentClassName : (parentContext != null ? parentContext.getCurrentClassName() : null);
    }

    @StillTesting
    public void setCurrentClassName(final String currentClassName) {
        if (currentClassName == null) return;
        this.currentClassName = currentClassName;
    }

    public String getCurrentFunctionName() {
        return currentFunctionName != null ? currentFunctionName : (parentContext != null ? parentContext.getCurrentFunctionName() : null);
    }

    public void setCurrentFunctionName(final String currentFunctionName) {
        this.currentFunctionName = currentFunctionName;
    }

    public List<String> getCurrentAnnotationNames() {
        return currentAnnotationNames;
    }

    public void addCurrentAnnotationName(final String currentAnnotationName) {
        this.currentAnnotationNames.add(currentAnnotationName);
    }

    public void removeCurrentAnnotationName(final String currentAnnotationName) {
        this.currentAnnotationNames.remove(currentAnnotationName);
    }

    public Context highest() {
        Context c = this;
        while (c.parentContext != null) {
            c = c.parentContext;
        }
        return c;
    }

    public void mergeContext(final Context source) {
        if (source == null) return;

        source.variables.forEach((key, value) -> {
            if (!this.variables.containsKey(key)) {
                this.variables.put(key, value);
            }
        });

        source.functions.forEach((key, list) -> {
            List<FunctionDefinition> targetFunctions = this.functions.computeIfAbsent(key, k -> new ArrayList<>());
            for (FunctionDefinition function : list) {
                if (!targetFunctions.contains(function)) {
                    targetFunctions.add(function);
                }
            }
        });


        for (final Map.Entry<String, List<FunctionDefinition>> entry : source.functions.entrySet()) {
            final String key = entry.getKey();
            final List<FunctionDefinition> list = entry.getValue();
            final List<FunctionDefinition> targetFunctions = this.functions.computeIfAbsent(key, k -> new ArrayList<>());
            for (final FunctionDefinition function : list) {
                if (!targetFunctions.contains(function)) {
                    targetFunctions.add(function);
                }
            }
        }

        source.classes.forEach(this.classes::putIfAbsent);

        source.annotations.forEach(this.annotations::putIfAbsent);

        this.natives.addAll(source.natives);

        this.currentAnnotationNames.addAll(source.currentAnnotationNames);
    }

    public Context parentContext() {
        return parentContext != null ? parentContext : this;
    }

    @Override
    public String toString() {
        return String.format("Context{%n" +
                        "variables=%s,%n" +
                        "functions=%s,%n" +
                        "classes=%s,%n" +
                        "annotations=%s,%n" +
                        "natives=%s,%n" +
                        "currentAnnotationNames=%s,%n" +
                        "currentClassName='%s',%n" +
                        "currentFunctionName='%s',%n" +
                        "parentContext=%s%n" +
                        "}",
                variables, functions, classes, annotations, natives,
                currentAnnotationNames, currentClassName, currentFunctionName, parentContext);
    }
}