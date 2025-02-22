package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class WhileNode extends ASTNode {

    private ASTNode condition;
    private BlockNode block;

    public WhileNode(final ASTNode condition, final BlockNode block) {
        this.condition = condition;
        this.block = block;
    }

    public WhileNode() {
        super();
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("WhileNode:\n");
        sb.append(indent).append("  Condition:\n");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Block:\n");
        if (block != null) {
            block.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(condition, version);
        out.writeNode(block, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.condition = in.readNode(version);
        this.block = (BlockNode) in.readNode(version);
    }
}