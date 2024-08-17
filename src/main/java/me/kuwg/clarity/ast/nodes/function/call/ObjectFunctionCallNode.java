package me.kuwg.clarity.ast.nodes.function.call;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ObjectFunctionCallNode extends ASTNode {

    private String caller;
    private String called;
    private List<ASTNode> params;

    public ObjectFunctionCallNode(final String caller, final String called, final List<ASTNode> params) {
        this.caller = caller;
        this.called = called;
        this.params = params;
    }

    public ObjectFunctionCallNode(){
    }

    public final String getCaller() {
        return caller;
    }

    public final String getCalled() {
        return called;
    }

    public final List<ASTNode> getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("ObjectFunctionCall: ").append(caller).append(".").append(called).append("(");

        if (params.isEmpty()) {
            sb.append(")");
        } else {
            sb.append("\n");
            String paramIndent = indent + "    ";
            for (ASTNode param : params) {
                param.print(sb, paramIndent);
                sb.append("\n");
            }
            sb.append(indent).append(")");
        }
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(caller);
        out.writeString(called);
        out.writeNodeList(params);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.caller = in.readString();
        this.called = in.readString();
        this.params = in.readNodeListNoCast();
    }
}