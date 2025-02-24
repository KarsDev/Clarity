package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class VirtualFunctionDeclarationNode extends ASTNode {
    protected String functionName;
    private String typeDefault;
    private boolean isAsync;
    protected List<ParameterNode> parameterNodes;

    public VirtualFunctionDeclarationNode(final String functionName, final String typeDefault, final boolean isAsync, final List<ParameterNode> parameterNodes) {
        this.functionName = functionName;
        this.typeDefault = typeDefault;
        this.isAsync = isAsync;
        this.parameterNodes = parameterNodes;
    }

    public VirtualFunctionDeclarationNode() {
        super();
    }

    public final String getFunctionName() {
        return functionName;
    }

    public final String getTypeDefault() {
        return typeDefault;
    }


    public final boolean isAsync() {
        return isAsync;
    }

    public final List<ParameterNode> getParameterNodes() {
        return parameterNodes;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Virtual Function: ").append(functionName).append(typeDefault != null ? " (" + typeDefault + ")" : "").append("\n");


        if (isAsync) {
            sb.append(indent).append("  Type: Async\n");
        }

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
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(functionName);
        out.writeString(String.valueOf(typeDefault));
        out.writeBoolean(isAsync);
        out.writeNodeList(parameterNodes);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.functionName = in.readString();
        this.typeDefault = in.readString();
        if (this.typeDefault.equals("null")) this.typeDefault = null;
        this.isAsync = in.readBoolean();
        this.parameterNodes = in.readNodeListNoCast(version);
    }
}
