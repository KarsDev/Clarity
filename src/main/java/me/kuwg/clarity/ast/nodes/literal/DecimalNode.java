package me.kuwg.clarity.ast.nodes.literal;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class DecimalNode extends ASTNode {

    private double value;

    public DecimalNode(final double value) {
        this.value = value;
    }

    public DecimalNode() {
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
    public void save(final ASTOutputStream out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.value = in.readDouble();
    }
}
