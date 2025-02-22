package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ParameterNode extends ASTNode {
    private String name;
    private boolean isLambda;

    public ParameterNode(final String name, final boolean isLambda) {
        this.name = name;
        this.isLambda = isLambda;
    }

    public ParameterNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    public final boolean isLambda() {
        return isLambda;
    }

    @Override
    public String toString() {
        return "ParameterNode{" +
                "isLambda=" + isLambda +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Parameter: ").append(name).append(isLambda ? "(lambda)" : " (default)").append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
        out.writeBoolean(isLambda);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.isLambda = in.readBoolean();
    }
}
