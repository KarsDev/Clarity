package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class FunctionDeclarationNode extends ASTNode {

    protected String functionName;
    private boolean isStatic;
    protected List<ParameterNode> parameterNodes;
    protected BlockNode block;

    public FunctionDeclarationNode(final String functionName, final boolean isStatic, final List<ParameterNode> parameterNodes, final BlockNode block) {
        this.functionName = functionName;
        this.isStatic = isStatic;
        this.parameterNodes = parameterNodes;
        this.block = block;
    }

    public FunctionDeclarationNode() {
        super();
    }

    public final String getFunctionName() {
        return functionName;
    }

    public final boolean isStatic() {
        return isStatic;
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
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(functionName);
        out.writeBoolean(isStatic);
        out.writeNodeList(parameterNodes);
        out.writeNode(block);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.functionName = in.readString();
        this.isStatic = in.readBoolean();
        this.parameterNodes = in.readNodeListNoCast();
        this.block = (BlockNode) in.readNode();
    }
}
