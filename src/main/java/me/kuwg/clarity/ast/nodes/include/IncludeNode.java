package me.kuwg.clarity.ast.nodes.include;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IncludeNode extends ASTNode implements PreInterpretable {
    private String name;
    private BlockNode included;
    private boolean isNative;

    public IncludeNode(final String name , final BlockNode included, final boolean isNative) {
        this.name = name;
        this.included = included;
        this.isNative = isNative;
    }

    public IncludeNode() {
        super();
    }

    public final String getName() {
        return name;
    }

    public final BlockNode getBlock() {
        return included;
    }

    public final boolean isNative() {
        return isNative;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Include (").append(name).append("):\n");
        if (isNative) sb.append(indent).append("    ").append("(native)");
        included.print(sb, included + "  ");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(included);
        out.writeBoolean(isNative);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.included = (BlockNode) in.readNode(version);
        this.isNative = in.readBoolean();
    }

    @Override
    public String toString() {
        return "IncludeNode{" +
                "name='" + name + '\'' +
                ", included=" + included +
                ", isNative=" + isNative +
                '}';
    }
}
