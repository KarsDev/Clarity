package me.kuwg.clarity.ast.nodes.function;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;
import org.omg.CORBA.ParameterMode;

import java.io.IOException;

public class ParameterNode extends ASTNode {
    private String name;

    public ParameterNode(final String name) {
        this.name = name;
    }

    public ParameterNode() {
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

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.name = in.readString();
    }
}
