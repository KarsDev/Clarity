package me.kuwg.clarity.ast.nodes.variable.get;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class VariableReferenceNode extends ASTNode {
    private String name;

    public VariableReferenceNode(final String name) {
        this.name = name;
    }

    public VariableReferenceNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Reference:\n");
        sb.append(indent).append("  Name: ").append(name).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
    }

    @Override
    public String toString() {
        return "VariableReferenceNode{" +
                "name='" + name + '\'' +
                '}';
    }
}
