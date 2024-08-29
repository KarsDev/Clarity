package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.Map;

public class ClassDefinition extends ObjectType {

    public final Map<String, VariableDefinition> staticVariables = new HashMap<>();
    public final Map<String, FunctionDefinition> staticFunctions = new HashMap<>();

    private final String name;
    private final ClassDefinition inheritedClass;
    private final FunctionDefinition constructor;
    private final BlockNode body;
    private final boolean isNative;

    public ClassDefinition(final String name, final ClassDefinition inheritedClass, final FunctionDefinition constructor, final BlockNode body, final boolean isNative) {
        this.name = name;
        this.inheritedClass = inheritedClass;
        this.constructor = constructor;
        this.body = body;
        this.isNative = isNative;
    }

    public final String getName() {
        return name;
    }

    public final ClassDefinition getInheritedClass() {
        return inheritedClass;
    }

    public final FunctionDefinition getConstructor() {
        return constructor;
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
                ", constructor=" + constructor +
                ", body=" + body +
                ", isNative=" + isNative +
                '}';
    }
}
