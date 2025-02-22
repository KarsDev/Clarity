package me.kuwg.clarity.ast.nodes.clazz.cast;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
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
        if (type.isClass()) out.writeString(type.getValue());
        out.writeNode(casted);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.type = CastType.ofOrdinal((in.readVarInt()));
        if (this.type.isClass()) this.type.setValue(in.readString());
        this.casted = in.readNode(version);
    }

    public final CastType getType() {
        return type;
    }

    public final ASTNode getCasted() {
        return casted;
    }
}
