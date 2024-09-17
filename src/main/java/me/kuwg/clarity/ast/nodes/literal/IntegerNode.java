package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IntegerNode extends AbstractNumberNode {

    private int value;

    public IntegerNode(final int value) {
        this.value = value;
    }

    public IntegerNode() {
        super();
    }

    public final int getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Integer:\n");
        sb.append(indent).append("  Value: ").append(value).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeInt(value);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.value = in.readInt();
    }

    @Override
    public String toString() {
        return "IntegerNode{" +
                "value=" + value +
                '}';
    }
}
