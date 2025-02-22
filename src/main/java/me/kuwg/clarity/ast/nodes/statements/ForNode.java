package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ForNode extends ASTNode implements PreInterpretable {

    private ASTNode declaration;
    private ASTNode condition;
    private ASTNode incrementation;
    private BlockNode block;

    public ForNode(final ASTNode declaration, final ASTNode condition, final ASTNode incrementation, final BlockNode block) {
        this.declaration = declaration;
        this.condition = condition;
        this.incrementation = incrementation;
        this.block = block;
    }

    public ForNode() {
        super();
    }

    public final ASTNode getDeclaration() {
        return declaration;
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final ASTNode getIncrementation() {
        return incrementation;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("ForNode:\n");
        sb.append(indent).append("  Declaration:\n");
        if (declaration != null) {
            declaration.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Condition:\n");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Incrementation:\n");
        if (incrementation != null) {
            incrementation.print(sb, indent + "    ");
        }
        sb.append(indent).append("  Block:\n");
        if (block != null) {
            block.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(declaration, version);
        out.writeNode(condition, version);
        out.writeNode(incrementation, version);
        out.writeNode(block, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.declaration = in.readNode(version);
        this.condition = in.readNode(version);
        this.incrementation = in.readNode(version);
        this.block = (BlockNode) in.readNode(version);
    }
}