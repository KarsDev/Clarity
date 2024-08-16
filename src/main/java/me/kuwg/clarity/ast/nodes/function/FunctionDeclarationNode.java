package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class FunctionDeclarationNode extends ASTNode {

    private String functionName;
    private List<ParameterNode> parameterNodes;
    private BlockNode block;

    public FunctionDeclarationNode(final String functionName, final List<ParameterNode> parameterNodes, final BlockNode block) {
        this.functionName = functionName;
        this.parameterNodes = parameterNodes;
        this.block = block;
    }

    public FunctionDeclarationNode() {
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
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(functionName);
        out.writeNodeList(parameterNodes);
        out.writeNode(block);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.functionName = in.readString();
        this.parameterNodes = (List<ParameterNode>) in.readNodeList();
        this.block = (BlockNode) in.readNode();
    }
}
