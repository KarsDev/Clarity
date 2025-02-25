package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.ast.nodes.function.declare.VirtualFunctionDeclarationNode;
import me.kuwg.clarity.library.objects.ObjectType;

import java.util.List;
import java.util.stream.Collectors;

public class VirtualFunctionDefinition extends ObjectType {
    private final String name;
    private final String typeDefault;
    private final boolean isAsync;
    private final List<String> params;

    public VirtualFunctionDefinition(final String name, final String typeDefault, final boolean isAsync, final List<String> params) {
        this.name = name;
        this.typeDefault = typeDefault;
        this.isAsync = isAsync;
        this.params = params;
    }

    public VirtualFunctionDefinition(final VirtualFunctionDeclarationNode node) {
        this(
                node.getFunctionName(),
                node.getTypeDefault(),
                node.isAsync(),
                node.getParameterNodes().stream().map(ParameterNode::getName).collect(Collectors.toList())
        );
    }

    public final String getName() {
        return name;
    }

    public final String getTypeDefault() {
        return typeDefault;
    }

    public final boolean isAsync() {
        return isAsync;
    }

    public final List<String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "VirtualFunctionDefinition{" +
                "isAsync=" + isAsync +
                ", name='" + name + '\'' +
                ", typeDefault='" + typeDefault + '\'' +
                ", params=" + params +
                '}';
    }
}
