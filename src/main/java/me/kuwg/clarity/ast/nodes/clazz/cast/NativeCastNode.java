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
        out.writeVarInt(type.ordinal());
        if (type == CastType.CLASS) out.writeString(type.getValue());
        out.writeNode(casted);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.type = CastType.VALUES[(in.readVarInt())];
        if (this.type == CastType.CLASS) this.type.setValue(in.readString());
        this.casted = in.readNode();
    }

    public final CastType getType() {
        return type;
    }

    public final ASTNode getCasted() {
        return casted;
    }

}
