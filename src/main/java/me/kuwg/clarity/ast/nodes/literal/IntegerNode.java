package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        final String longAsString = Long.toString(value);

        final boolean writingAsString = longAsString.getBytes(StandardCharsets.UTF_8).length << 3 < 64;

        out.writeBoolean(writingAsString);

        if (writingAsString) {
            out.writeString(longAsString);
        } else {
            out.writeLong(value);
        }
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        if (version.isOlderThan(CompilerVersion.V1_0)) {
            this.value = in.readLong();
            return;
        }

        if (!in.readBoolean()) {
            this.value = in.readLong();
            return;
        }

        this.value = Long.parseLong(in.readString());
    }

    @Override
    public String toString() {
        return "IntegerNode{" +
                "value=" + value +
                '}';
    }

}
