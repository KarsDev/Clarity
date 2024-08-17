package me.kuwg.clarity.ast.nodes.include;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class IncludeNode extends ASTNode {
    private BlockNode included;

    public IncludeNode(final BlockNode included) {
        this.included = included;
    }

    public IncludeNode() {
        super();
    }

    public final BlockNode getIncluded() {
        return included;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Include:\n");
        included.print(sb, included + "  ");
    }

    @Override
    public void save(final ASTOutputStream out) throws IOException {
        out.writeNode(included);
    }

    @Override
    public void load(final ASTInputStream in) throws IOException {
        included = (BlockNode) in.readNode();
    }
}
