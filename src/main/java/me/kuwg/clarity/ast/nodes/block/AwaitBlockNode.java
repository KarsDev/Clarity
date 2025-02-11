package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class AwaitBlockNode extends ASTNode implements PreInterpretable {

    private BlockNode block;

    public AwaitBlockNode(final BlockNode block) {
        this.block = block;
    }

    public AwaitBlockNode() {
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Async Block:\n");
        block.print(sb, indent + "  ");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeNode(block);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.block = (BlockNode) in.readNode();
    }
}
