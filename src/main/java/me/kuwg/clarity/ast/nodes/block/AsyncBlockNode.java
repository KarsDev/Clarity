package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class AsyncBlockNode extends ASTNode {

    private String name;
    private BlockNode block;

    public AsyncBlockNode(final String name, final BlockNode block) {
        this.name = name;
        this.block = block;
    }

    public AsyncBlockNode() {
    }

    public final BlockNode getBlock() {
        return block;
    }

    public final String getName() {
        return name;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Async Block:\n");
        sb.append(indent).append("  ").append("Name: ").append(name);
        block.print(sb, indent + "  ");
    }

    @Override
    protected void save0(final ASTOutputStream out) throws IOException {
        out.writeString(name);
        out.writeNode(block);
    }

    @Override
    protected void load0(final ASTInputStream in) throws IOException {
        this.name = in.readString();
        this.block = (BlockNode) in.readNode();
    }
}
