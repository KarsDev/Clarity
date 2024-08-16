package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ParameterNode extends ASTNode {
    private final String name;

    public ParameterNode(final String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Parameter: ").append(name).append("\n");
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeString(name);
    }
}
