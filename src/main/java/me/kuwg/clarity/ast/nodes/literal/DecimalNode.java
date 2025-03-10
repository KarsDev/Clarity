package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

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
        out.writeOptimalDouble(value);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.value = in.readOptimalDouble(version);
    }

    @Override
    public String toString() {
        return "DecimalNode{" +
                "value=" + value +
                '}';
    }
}
