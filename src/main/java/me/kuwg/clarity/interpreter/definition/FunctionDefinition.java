package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.interpreter.types.ObjectType;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionDefinition extends ObjectType {
    private final String name;
    private final List<String> params;
    private final BlockNode block;

    public FunctionDefinition(final String name, final List<String> params, final BlockNode block) {
        this.name = name;
        this.params = params;
        this.block = block;
    }

    public FunctionDefinition(final FunctionDeclarationNode node) {
        this(
                node.getFunctionName(),
                node.getParameterNodes().stream().map(ParameterNode::getName).collect(Collectors.toList()),
                node.getBlock()
        );
    }

    public final String getName() {
        return name;
    }

    public final List<String> getParams() {
        return params;
    }

    public final BlockNode getBlock() {
        return block;
    }
}
