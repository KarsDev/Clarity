package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IntegerNode extends AbstractNumberNode {

    public static final IntegerNode ZERO = new IntegerNode(0);
    public static final IntegerNode ONE = new IntegerNode(1);

    private long value;

    public IntegerNode(final long value) {
        this.value = value;
    }

    public IntegerNode() {
        super();
    }

    public final long getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Integer:\n");
        sb.append(indent).append("  Value: ").append(value).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeOptimalLong(value);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.value = in.readOptimalLong(version);
    }

    @Override
    public String toString() {
        return "IntegerNode{" +
                "value=" + value +
                '}';
    }

}
