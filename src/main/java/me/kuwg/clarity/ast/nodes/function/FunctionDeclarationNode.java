package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.util.List;

public class FunctionDeclarationNode extends ASTNode {

    private final String functionName;
    private final List<ParameterNode> parameterNodes;
    private final BlockNode block;

    public FunctionDeclarationNode(final String functionName, final List<ParameterNode> parameterNodes, final BlockNode block) {
        this.functionName = functionName;
        this.parameterNodes = parameterNodes;
        this.block = block;
    }

    public final String getFunctionName() {
        return functionName;
    }

    public final List<ParameterNode> getParameterNodes() {
        return parameterNodes;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function: ").append(functionName).append("\n");

        sb.append(indent).append("Parameters: ");
        if (parameterNodes.isEmpty()) {
            sb.append("None\n");
        } else {
            sb.append("\n");
            String paramIndent = indent + "    ";
            for (ParameterNode param : parameterNodes) {
                param.print(sb, paramIndent);
            }
        }

        sb.append(indent).append("Body:\n");
        block.print(sb, indent + "    ");
    }

    @Override
    public void save(final ASTOutputStream out) {

    }
}
