package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ObjectType;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionDefinition extends ObjectType {
    private final String name;
    private final String typeDefault;
    private final boolean isStatic;
    private final List<String> params;
    private final BlockNode block;

    public FunctionDefinition(final String name, final String typeDefault, final boolean isStatic, final List<String> params, final BlockNode block) {
        this.name = name;
        this.typeDefault = typeDefault;
        this.isStatic = isStatic;
        this.params = params;
        this.block = block;
    }

    public FunctionDefinition(final FunctionDeclarationNode node) {
        this(
                node.getFunctionName(),
                node.getTypeDefault(),
                node.isStatic(),
                node.getParameterNodes().stream().map(ParameterNode::getName).collect(Collectors.toList()),
                node.getBlock()
        );
    }

    public final String getName() {
        return name;
    }

    public final String getTypeDefault() {
        return typeDefault;
    }

    public final boolean isStatic() {
        return isStatic;
    }

    public final List<String> getParams() {
        return params;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "FunctionDefinition{" +
                "name='" + name + '\'' +
                ", isStatic=" + isStatic +
                ", params=" + params +
                ", block=" + block +
                '}';
    }
}
