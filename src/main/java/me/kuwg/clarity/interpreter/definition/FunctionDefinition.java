package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionDefinition extends ObjectType {
    private final String name;
    private final String typeDefault;
    private final boolean isStatic, isConst, isLocal, isAsync;
    private final List<String> params;
    private final BlockNode block;

    public FunctionDefinition(final String name, final String typeDefault, final boolean isStatic, final boolean isConst, final boolean isLocal, final boolean isAsync, final List<String> params, final BlockNode block) {
        this.name = name;
        this.typeDefault = typeDefault;
        this.isStatic = isStatic;
        this.isConst = isConst;
        this.isLocal = isLocal;
        this.isAsync = isAsync;
        this.params = params;
        this.block = block;
    }

    public FunctionDefinition(final FunctionDeclarationNode node) {
        this(
                node.getFunctionName(),
                node.getTypeDefault(),
                node.isStatic(),
                node.isConst(),
                node.isLocal(),
                node.isAsync(),
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

    public final boolean isConst() {
        return isConst;
    }

    public final boolean isLocal() {
        return isLocal;
    }

    public final boolean isAsync() {
        return isAsync;
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
                "block=" + block +
                ", name='" + name + '\'' +
                ", typeDefault='" + typeDefault + '\'' +
                ", isStatic=" + isStatic +
                ", isConst=" + isConst +
                ", isLocal=" + isLocal +
                ", isAsync=" + isAsync +
                ", params=" + params +
                '}';
    }
}
