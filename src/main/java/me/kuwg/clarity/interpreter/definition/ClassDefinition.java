package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassDefinition extends ObjectType {

    public final Map<String, VariableDefinition> staticVariables = new HashMap<>();
    public final Map<String, FunctionDefinition> staticFunctions = new HashMap<>();

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
