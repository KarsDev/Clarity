package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class DeleteFunctionNode extends ASTNode {
    private String name;
    private ASTNode params;

    public DeleteFunctionNode(final String name, final ASTNode params) {
        this.name = name;
        this.params = params;
    }

    public DeleteFunctionNode() {
    }

    public final String getName() {
        return name;
    }

    public final ASTNode getParams() {
        return params;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function Delete: \n");
        sb.append(indent).append(indent).append("Name: ").append(name).append("\n");
        sb.append(indent).append(indent).append("Params: \n");
        params.print(sb, indent + indent + indent);
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(params);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.params = in.readNode();
    }
}
