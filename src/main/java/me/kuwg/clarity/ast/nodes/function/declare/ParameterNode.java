package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class ParameterNode extends ASTNode {
    private String name;

    public ParameterNode(final String name) {
        this.name = name;
    }

    public ParameterNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ParameterNode{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Parameter: ").append(name).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
    }
}
