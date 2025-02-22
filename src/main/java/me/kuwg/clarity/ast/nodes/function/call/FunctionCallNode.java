package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class FunctionCallNode extends ASTNode {

    private ASTNode caller;
    private List<ASTNode> params;

    public FunctionCallNode(final ASTNode caller, final List<ASTNode> params) {
        this.caller = caller;
        this.params = params;
    }

    public FunctionCallNode() {
    }

    public final ASTNode getCaller() {
        return caller;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function Call: ").append(caller).append("\n");
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
        out.writeNode(caller);
        out.writeNodeList(params);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.caller = in.readNode(version);
        this.params = in.readNodeListNoCast(version);
    }

    @Override
    public String toString() {
        return "FunctionCallNode{" +
                "caller=" + caller +
                ", params=" + params +
                '}';
    }
}
