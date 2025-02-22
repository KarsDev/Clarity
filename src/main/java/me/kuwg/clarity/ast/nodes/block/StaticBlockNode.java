package me.kuwg.clarity.ast.nodes.block;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class StaticBlockNode extends ASTNode implements PreInterpretable {

    private BlockNode block;
    private boolean isAsync;

    public StaticBlockNode(final BlockNode block, final boolean isAsync) {
        this.block = block;
        this.isAsync = isAsync;
    }

    public StaticBlockNode() {
    }

    public final BlockNode getBlock() {
        return block;
    }

    public final boolean isAsync() {
        return isAsync;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Static Block: ").append(isAsync ? "(async)": "").append("\n");
        block.print(sb, indent + "   ");
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(block, version);
        out.writeBoolean(isAsync);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.block = (BlockNode) in.readNode(version);
        this.isAsync = in.readBoolean();
    }
}
