package me.kuwg.clarity.ast.nodes.clazz.cast;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class NativeCastNode extends ASTNode {
    private CastType type;
    private ASTNode casted;

    public NativeCastNode(final CastType type, final ASTNode casted) {
        this.type = type;
        this.casted = casted;
    }

    public NativeCastNode() {
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Native Cast: ").append("\n");
        sb.append(indent).append("  ").append("Type: ").append(type);
        sb.append(indent).append("  ").append("Casted: ").append(casted);
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeByte(type.toStreamByte());
        out.writeNode(casted);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.type = CastType.fromStreamByte(in.readByte());
        this.casted = in.readNode();
    }

    public final CastType getType() {
        return type;
    }

    public final ASTNode getCasted() {
        return casted;
    }

    public enum CastType {
        FLOAT,
        INT;

        private byte toStreamByte() {
            switch (this) {
                case FLOAT:
                    return 0x0;

                case INT:
                    return 0x1;

                default:
                    throw new RuntimeException("Unreachable");
            }
        }
        private static CastType fromStreamByte(final byte val) {
            switch (val) {
                case 0x0:
                    return FLOAT;

                case 0x1:
                    return INT;

                default:
                    throw new RuntimeException("Unreachable");
            }
        }
    }
}
