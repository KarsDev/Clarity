package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static me.kuwg.clarity.compiler.ASTData.getVarIntBits;

public class DecimalNode extends AbstractNumberNode {

    private double value;

    public DecimalNode(final double value) {
        this.value = value;
    }

    public DecimalNode() {
        super();
    }

    public final double getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Decimal:\n");
        sb.append(indent).append("  Value: ").append(value).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        final String doubleAsString = Double.toString(value);

        final boolean writingAsString = doubleAsString.getBytes(StandardCharsets.UTF_8).length << 3 + getVarIntBits(doubleAsString.length()) < 64;

        out.writeBoolean(writingAsString);

        if (writingAsString) {
            out.writeString(doubleAsString);
        } else {
            out.writeDouble(value);
        }
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        if (version.isOlderThan(CompilerVersion.V1_0)) {
            this.value = in.readDouble();
            return;
        }

        if (!in.readBoolean()) {
            this.value = in.readDouble();
            return;
        }

        this.value = Double.parseDouble(in.readString());
    }

    @Override
    public String toString() {
        return "DecimalNode{" +
                "value=" + value +
                '}';
    }
}
