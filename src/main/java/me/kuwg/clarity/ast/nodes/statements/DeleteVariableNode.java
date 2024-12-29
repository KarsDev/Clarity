package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class DeleteVariableNode extends ASTNode {
    private String name;

    public DeleteVariableNode(final String name) {
        this.name = name;
    }

    public DeleteVariableNode() {
    }

    public final String getName() {
        return name;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Delete: \n").append(indent).append(indent).append(name).append("\n");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
    }
}
