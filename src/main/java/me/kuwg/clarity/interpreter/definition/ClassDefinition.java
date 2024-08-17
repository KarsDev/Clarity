package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

public class ClassDefinition extends ObjectType {
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
}
