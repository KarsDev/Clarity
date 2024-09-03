package me.kuwg.clarity.ast.nodes.member;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class MemberFunctionCallNode extends ASTNode {

    private ASTNode caller;
    private String name;
    private List<ASTNode> params;

    public MemberFunctionCallNode(final ASTNode caller, final String name, final List<ASTNode> params) {
        this.caller = caller;
        this.name = name;
        this.params = params;
    }

    public MemberFunctionCallNode() {
    }

    public final ASTNode getCaller() {
        return caller;
    }

    public final String getName() {
        return name;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function Call: ").append("\n");
        final String paramIndent = indent + indent + "    ";
        sb.append(indent).append(indent).append("Caller:").append("\n");
        caller.print(sb, paramIndent);
        sb.append(indent).append(indent).append("Name: ").append(name).append("\n");
        sb.append(indent).append(indent).append("Parameters: ");
        if (params.isEmpty()) {
            sb.append("None\n");
        } else {
            sb.append("\n");
            for (ASTNode param : params) {
                param.print(sb, paramIndent);
            }
        }
    }


    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(caller);
        out.writeString(name);
        out.writeNodeList(params);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.caller = in.readNode();
        this.name = in.readString();
        this.params = in.readNodeListNoCast();
    }

    @Override
    public String toString() {
        return "MemberFunctionCallNode{" +
                "caller=" + caller +
                ", name='" + name + '\'' +
                ", params=" + params +
                '}';
    }
}
