package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.*;

public class ClassDefinition extends ObjectType {

    public final Map<String, VariableDefinition> staticVariables = new HashMap<>();
    public final List<FunctionDefinition> staticFunctions = new ArrayList<>();

    private final String name;
    private final boolean isConstant;
    private final ClassDefinition inheritedClass;
    private final FunctionDefinition[] constructors;
    private final BlockNode body;
    private final boolean isNative;

    public ClassDefinition(final String name, final boolean isConstant, final ClassDefinition inheritedClass, final FunctionDefinition[] constructors, final BlockNode body, final boolean isNative) {
        this.name = name;
        this.isConstant = isConstant;
        this.inheritedClass = inheritedClass;
        this.constructors = constructors;
        this.body = body;
        this.isNative = isNative;
    }

    public final String getName() {
        return name;
    }

    public final boolean isConstant() {
        return isConstant;
    }

    public final ClassDefinition getInheritedClass() {
        return inheritedClass;
    }

    public final FunctionDefinition[] getConstructors() {
        return constructors;
    }

    public final BlockNode getBody() {
        return body;
    }

    public final boolean isNative() {
        return isNative;
    }

    public final FunctionDefinition getStaticFunction(final String name, final int params) {
        for (final FunctionDefinition staticFunction : staticFunctions) {
            if (staticFunction.getName().equals(name) && staticFunction.getParams().size() == params) {
                return staticFunction;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "staticVariables=" + staticVariables +
                ", staticFunctions=" + staticFunctions +
                ", name='" + name + '\'' +
                ", inheritedClass='" + inheritedClass + '\'' +
                ", constructors=" + Arrays.toString(constructors) +
                ", body=" + body +
                ", isNative=" + isNative +
                '}';
    }
}
