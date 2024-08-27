package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class LocalFunctionCallNode extends ASTNode {
    private String functionName;
    private List<ASTNode> params;

    public LocalFunctionCallNode(final String functionName, final List<ASTNode> params) {
        this.functionName = functionName;
        this.params = params;
    }

    public LocalFunctionCallNode() {
    }

    public final String getName() {
        return functionName;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function Call: ").append(functionName).append("\n");
        sb.append(indent).append("Parameters: ");
        if (params.isEmpty()) {
            sb.append("None\n");
        } else {
            sb.append("\n");
            String paramIndent = indent + "    ";
            for (ASTNode param : params) {
                param.print(sb, paramIndent);
            }
        }
    }


    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(functionName);
        out.writeNodeList(params);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.functionName = in.readString();
        this.params = in.readNodeListNoCast();
    }
}
