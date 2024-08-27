package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class LiteralNode extends ASTNode {
    private String value;

    public LiteralNode(final String value) {
        this.value = value == null ? null : value.length() > 2 ? value.substring(1, value.length() - 1) : "";
    }

    public LiteralNode() {
        super();
    }

    public final String getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Literal:\n");
        sb.append(indent).append("  Value: ").append(value).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(value);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.value = in.readString();
    }

    @Override
    public String toString() {
        return "LiteralNode{" +
                "value='" + value + '\'' +
                '}';
    }
}
