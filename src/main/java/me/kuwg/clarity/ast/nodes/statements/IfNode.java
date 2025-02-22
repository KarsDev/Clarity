package me.kuwg.clarity.ast.nodes.statements;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IfNode extends ASTNode {

    private ASTNode condition;
    private BlockNode ifBlock;
    private List<IfNode> elseIfStatements;
    private BlockNode elseBlock;

    public IfNode(final ASTNode condition, final BlockNode ifBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseIfStatements = new ArrayList<>();
        this.elseBlock = null;
    }

    public IfNode() {
        super();
        this.elseIfStatements = new ArrayList<>();
    }

    public final ASTNode getCondition() {
        return condition;
    }

    public final BlockNode getIfBlock() {
        return ifBlock;
    }

    public final List<IfNode> getElseIfStatements() {
        return elseIfStatements;
    }

    public final BlockNode getElseBlock() {
        return elseBlock;
    }

    public void addElseIfStatement(final IfNode elseIfBlock) {
        this.elseIfStatements.add(elseIfBlock);
    }

    public void setElseBlock(final BlockNode elseBlock) {
        this.elseBlock = elseBlock;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("IfNode:\n");
        sb.append(indent).append("  Condition:\n");
        if (condition != null) {
            condition.print(sb, indent + "    ");
        }
        sb.append(indent).append("  If Block:\n");
        if (ifBlock != null) {
            ifBlock.print(sb, indent + "    ");
        }
        for (final IfNode elseIf : elseIfStatements) {
            sb.append(indent).append("  Else If:\n");
            elseIf.print(sb, indent + "    ");
        }
        if (elseBlock != null) {
            sb.append(indent).append("  Else Block:\n");
            elseBlock.print(sb, indent + "    ");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(condition, version);
        out.writeNode(ifBlock, version);
        out.writeNodeList(elseIfStatements, version);
        out.writeNode(elseBlock, version);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.condition = in.readNode(version);
        this.ifBlock = (BlockNode) in.readNode(version);
        this.elseIfStatements = in.readNodeListNoCast(version);
        this.elseBlock = (BlockNode) in.readNode(version);
    }
}
