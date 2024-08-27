package me.kuwg.clarity.ast.nodes.include;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IncludeNode extends ASTNode {
    private BlockNode included;
    private boolean isNative;

    public IncludeNode(final BlockNode included, final boolean isNative) {
        this.included = included;
        this.isNative = isNative;
    }

    public IncludeNode() {
        super();
    }

    public final BlockNode getIncluded() {
        return included;
    }

    public final boolean isNative() {
        return isNative;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Include:\n");
        included.print(sb, included + "  ");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(included);
        out.writeBoolean(isNative);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.included = (BlockNode) in.readNode();
        this.isNative = in.readBoolean();
    }
}
