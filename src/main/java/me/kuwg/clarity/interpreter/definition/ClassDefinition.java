package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.HashMap;
import java.util.Map;

public class ClassDefinition extends ObjectType {

    public final Map<String, VariableDefinition> staticVariables = new HashMap<>();
    public final Map<String, FunctionDefinition> staticFunctions = new HashMap<>();

    private final String name;
    private final FunctionDefinition constructor;
    private final BlockNode body;

    public ClassDefinition(final String name, final FunctionDefinition constructor, final BlockNode body) {
        this.name = name;
        this.constructor = constructor;
        this.body = body;
    }

    public final String getName() {
        return name;
    }

    public final FunctionDefinition getConstructor() {
        return constructor;
    }

    public final BlockNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ClassDefinition{" +
                "name='" + name + '\'' +
                ", constructor=" + constructor +
                ", body=" + body +
                '}';
    }
}
