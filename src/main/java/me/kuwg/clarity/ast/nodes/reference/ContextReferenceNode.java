package me.kuwg.clarity.ast.nodes.reference;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ContextReferenceNode extends ASTNode {

    private ReferenceType type;

    public ContextReferenceNode(final ReferenceType type) {
        this.type = type;
    }

    public ContextReferenceNode() {
    }

    public final ReferenceType getType() {
        return type;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Context Reference:\n");
        sb.append(indent).append("  Type: ").append(type).append("\n");
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeInt(type.toIntValue());
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        this.type = ReferenceType.fromIntValue(in.readInt());
    }


    public enum ReferenceType {
        LOCAL,
        VARIABLE;

        public static ReferenceType fromIntValue(final int i) {
            switch (i) {
                case 0:
                    return LOCAL;
                case 1:
                    return VARIABLE;
                default:
                    throw new UnsupportedOperationException("Unsupported Reference Type int value: " + i);
            }
        }

        public int toIntValue() {
            switch (this) {
                case LOCAL:
                    return 0;
                case VARIABLE:
                    return 1;
                default:
                    throw new UnsupportedOperationException("Unsupported Reference Type value: " + this);
            }
        }
    }
}
